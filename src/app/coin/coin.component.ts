import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import * as THREE from 'three';
import {BufferGeometry, Material, MathUtils, Texture} from 'three';
import {CoinRotation} from './coinRotation';
import {Observable} from 'rxjs';
import {CoinService} from '../core/services/coin/coin.service';

@Component({
  selector: 'app-coin',
  templateUrl: './coin.component.html',
  styleUrls: ['./coin.component.scss']
})
export class CoinComponent implements OnInit, AfterViewInit, OnChanges, OnDestroy {
  private static readonly FRAME_RATE = 60;
  private static readonly FLIP_TRIGGER_DELTA = 30;

  @Input('uncertaintyId') private uncertaintyId: string;
  @Input('style') private coinStyle: string;
  @Input('state') private coinState: CoinState;
  @ViewChild('rendererContainer') private rendererContainer: ElementRef;

  private renderer = CoinComponent.initRenderer();
  private scene = new THREE.Scene();
  private camera = this.initCamera();
  private object: THREE.Mesh = null;
  private animationMethod = null;
  private defaultCamera = null;
  private headTexture: THREE.Texture = null;
  private edgeTexture: THREE.Texture = null;
  private tailTexture: THREE.Texture = null;
  private rotation: CoinRotation = null;
  private interval = null;

  constructor(
    private coinService: CoinService) { }

  ngOnInit(): void {
    this.object = CoinComponent.initObject(this.coinStyle);
    this.scene.add(this.object);
  }

  ngAfterViewInit(): void {
    this.rotation = new CoinRotation(this.object, this.rendererContainer);
    this.renderer.domElement.style.height = '100%';
    this.renderer.domElement.style.width = '100%';
    this.rendererContainer.nativeElement.appendChild(this.renderer.domElement);
    this.defaultCamera = this.camera.clone(true);
    this.updateCoinStyle();
    this.updateCoinState();

    this.render();
    this.interval = setInterval( () => { this.render(); }, 1000 / CoinComponent.FRAME_RATE );
  }

  ngOnChanges(changes: SimpleChanges): void {
    for (const propName of Object.keys(changes)) {
      switch (propName) {
        case 'coinStyle':
          this.updateCoinStyle();
          break;
        case 'coinState':
          this.updateCoinState();
          break;
      }
    }
  }

  ngOnDestroy(): void {
    console.log('Cleaning up...');

    clearInterval(this.interval);
    this.disposeTextures();
    this.object?.geometry.dispose();
    this.scene.dispose();
    this.renderer.dispose();
  }

  @HostListener('mousedown', ['$event'])
  onComponentMouseDown(event): void {
    this.rotation?.onMouseDown(event);
  }

  @HostListener('document:mousemove', ['$event'])
  onDocumentMouseMove(event) {
    this.rotation?.onMouseMove(event);
  }

  @HostListener('document:mouseup', ['$event'])
  onDocumentMouseUp(event) {
    const [deltaX, deltaY] = this.rotation?.onMouseUp(event);
    if (deltaX == null) { return; }

    console.log('quarternion:', this.object.quaternion);
    console.log('delta:', deltaX, deltaY);

    this.coinService.updateCoinState(this.uncertaintyId, this.rotation.toCoinState());

    if (Math.abs(deltaX) > CoinComponent.FLIP_TRIGGER_DELTA || Math.abs(deltaY) > CoinComponent.FLIP_TRIGGER_DELTA) {
      this.flip();
    }
  }

  flip() {
    console.info('FLIP FLIP');
    this.coinService.flipCoin(this.uncertaintyId);

    // const newCoinState = this.rotation.toCoinState();
    // newCoinState.drag = 1.0;
    // newCoinState.interactive = false;
    // this.rotation.updateCoinState(newCoinState);
  }

  private updateCoinStyle(): void {
    if (this.object == null) {
      // ngOnChange triggered before ngInit, ignore.
      return;
    }

    console.log(`Updating coin style to ${this.coinStyle}`);
    this.disposeTextures();

    // materials
    this.headTexture = new THREE.TextureLoader().load('assets/img/coins/' + this.coinStyle + '/heads.png');
    this.headTexture.flipY = false;
    this.headTexture.wrapS = THREE.RepeatWrapping;
    this.headTexture.repeat.x = - 1;

    this.edgeTexture = new THREE.TextureLoader().load('assets/img/coins/' + this.coinStyle + '/edge.png');
    // edgeTexture.repeat.x = -1;
    this.edgeTexture.repeat.x = 40;
    this.edgeTexture.wrapS = this.edgeTexture.wrapT = THREE.RepeatWrapping;

    this.tailTexture = new THREE.TextureLoader().load('assets/img/coins/' + this.coinStyle + '/tails.png');

    this.object.material = [
      new THREE.MeshBasicMaterial({map: this.edgeTexture}),
      new THREE.MeshBasicMaterial({map: this.tailTexture}),
      new THREE.MeshBasicMaterial({map: this.headTexture})
    ];

    this.coinService.updateCoinStyle(this.uncertaintyId, this.coinStyle);
  }

  private disposeTextures(): void {
    (this.object?.material as Material[]).forEach((mat) => mat.dispose());
    this.headTexture?.dispose();
    this.edgeTexture?.dispose();
    this.tailTexture?.dispose();
  }

  private updateCoinState(): void {
    this.rotation?.updateCoinState(this.coinState);
  }

  // @HostListener('window:resize', ['$event'])
  // onWindowResize(event) {
  //   this.renderer.setSize(event.target.innerWidth, event.target.innerHeight);
  // }

  private render() {
    this.resizeCanvasToDisplaySize();

    if (this.animationMethod != null) {
      this.animationMethod();
    }


    this.rotation.handleRotation();

    this.renderer.render(this.scene, this.camera);
  }


  private resizeCanvasToDisplaySize() {
    const canvas = this.renderer.domElement;
    // look up the size the canvas is being displayed
    const width = canvas.clientWidth;
    const height = canvas.clientHeight;

    // adjust displayBuffer size to match
    if (canvas.width !== width || canvas.height !== height) {
      // you must pass false here or three.js sadly fights the browser
      this.renderer.setSize(width, height, false);
      this.camera.aspect = width / height;
      this.camera.updateProjectionMatrix();

      // update any render target sizes here
    }
  }

  private initCamera(): THREE.PerspectiveCamera {
    const camera = new THREE.PerspectiveCamera(90, 2, 1, 1000);
    camera.position.set(0, 3.5, 5);
    camera.lookAt(this.scene.position);
    return camera;
  }

  private static initRenderer() {
    const renderer = new THREE.WebGLRenderer({
      antialias: true,
      alpha: true,
    });
    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFShadowMap;
    return renderer;
  }

  private static initObject(coinStyle: string): THREE.Mesh {
    // geometry
    const diameter = 2.5;
    const radialSegments = 50;
    const geometry = new THREE.CylinderGeometry(diameter, diameter, 0.3, radialSegments);

    // object
    const object = new THREE.Mesh(geometry, []);
    object.rotation.y = 1.5;

    return object;
  }
}
