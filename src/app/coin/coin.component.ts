import {AfterViewInit, Component, ElementRef, HostListener, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import * as THREE from 'three';
import {MathUtils} from 'three';
import {CoinRotation} from './coinRotation';
import {CoinState} from '../core/services/coin/coin.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-coin',
  templateUrl: './coin.component.html',
  styleUrls: ['./coin.component.scss']
})
export class CoinComponent implements OnInit, AfterViewInit, OnChanges {
  private static readonly FRAME_RATE = 60;
  private static readonly FLIP_TRIGGER_DELTA = 30;

  @Input('style') private coinStyle: string;
  @Input('state') private coinState: CoinState;
  @ViewChild('rendererContainer') private rendererContainer: ElementRef;

  private renderer = CoinComponent.initRenderer();
  private scene = new THREE.Scene();
  private camera = this.initCamera();
  private object: THREE.Mesh = null;
  private animationMethod = null;
  private defaultCamera = null;
  private rotation: CoinRotation = null;

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
    setInterval( () => { this.render(); }, 1000 / CoinComponent.FRAME_RATE );
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

  observeCoinState(): Observable<CoinState> {
    return this.rotation.observeCoinState();
  }

  @HostListener('mousedown', ['$event'])
  onComponentMouseDown(event) {
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
    if (Math.abs(deltaX) > CoinComponent.FLIP_TRIGGER_DELTA || Math.abs(deltaY) > CoinComponent.FLIP_TRIGGER_DELTA) {
      this.flip();
    }
  }

  flip() {
    console.log('FLIP FLIP');
  }

  private updateCoinStyle(): void {
    if (this.object == null) {
      // ngOnChange triggered before ngInit, ignore.
      return;
    }

    console.log(`Updating coin style to ${this.coinStyle}`);
    // materials
    const headTexture = new THREE.TextureLoader().load('assets/img/coins/' + this.coinStyle + '/heads.png');
    headTexture.flipY = false;
    headTexture.wrapS = THREE.RepeatWrapping;
    headTexture.repeat.x = - 1;

    const edgeTexture = new THREE.TextureLoader().load('assets/img/coins/' + this.coinStyle + '/edge.png');
    // edgeTexture.repeat.x = -1;
    edgeTexture.repeat.x = 40;
    edgeTexture.wrapS = edgeTexture.wrapT = THREE.RepeatWrapping;

    this.object.material = [
      new THREE.MeshBasicMaterial({map: edgeTexture}),
      new THREE.MeshBasicMaterial({map: new THREE.TextureLoader().load('assets/img/coins/' + this.coinStyle + '/tails.png')}),
      new THREE.MeshBasicMaterial({map: headTexture})
    ];
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
