import {AfterViewInit, Component, ElementRef, HostListener, OnInit, ViewChild} from '@angular/core';
import * as THREE from 'three';
import * as TrackballControls from 'three-trackballcontrols';
import {MathUtils} from 'three';
import clamp = MathUtils.clamp;
import {CoinRotation} from './coinRotation';

@Component({
  selector: 'app-coin',
  templateUrl: './coin.component.html',
  styleUrls: ['./coin.component.scss']
})
export class CoinComponent implements OnInit, AfterViewInit {
  private static readonly FLIP_TRIGGER_DELTA = 30;

  @ViewChild('rendererContainer') rendererContainer: ElementRef;

  coinStyle = 'germany';
  renderer = CoinComponent.initRenderer();
  scene = new THREE.Scene();
  camera = this.initCamera();
  object: THREE.Mesh = null;
  animationMethod = null;
  defaultCamera = null;
  rotation: CoinRotation = null;

  ngOnInit(): void {
    this.object = CoinComponent.initObject(this.coinStyle);
    this.scene.add(this.object);
  }

  ngAfterViewInit() {
    this.rotation = new CoinRotation(this.object, this.rendererContainer);
    this.renderer.domElement.style.height = '100%';
    this.renderer.domElement.style.width = '100%';
    this.rendererContainer.nativeElement.appendChild(this.renderer.domElement);
    this.defaultCamera = this.camera.clone(true);

    this.render();
    setInterval( () => { this.render(); }, 1000 / 60 );
  }

  @HostListener('document:mousedown', ['$event'])
  onDocumentMouseDown(event) {
    this.rotation?.onDocumentMouseDown(event);
  }

  @HostListener('document:mousemove', ['$event'])
  onDocumentMouseMove(event) {
    this.rotation?.onDocumentMouseMove(event);
  }

  @HostListener('document:mouseup', ['$event'])
  onDocumentMouseUp(event) {
    const [deltaX, deltaY] = this.rotation?.onDocumentMouseUp(event);
    console.log(deltaX, deltaY);
    if (Math.abs(deltaX) > CoinComponent.FLIP_TRIGGER_DELTA || Math.abs(deltaY) > CoinComponent.FLIP_TRIGGER_DELTA) {
      console.log('FLIP FLIP');
    }
  }

  flip() {

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

    // materials
    const headTexture = new THREE.TextureLoader().load('assets/img/coins/' + coinStyle + '/heads.png');
    headTexture.flipY = false;
    headTexture.wrapS = THREE.RepeatWrapping;
    headTexture.repeat.x = - 1;

    const edgeTexture = new THREE.TextureLoader().load('assets/img/coins/' + coinStyle + '/edge.png');
    // edgeTexture.repeat.x = -1;
    edgeTexture.repeat.x = 40;
    edgeTexture.wrapS = edgeTexture.wrapT = THREE.RepeatWrapping;

    const materials = [
      new THREE.MeshBasicMaterial({ map: edgeTexture }),
      new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load('assets/img/coins/' + coinStyle + '/tails.png') }),
      new THREE.MeshBasicMaterial({ map: headTexture })
    ];

    // object
    const object = new THREE.Mesh(geometry, materials);
    object.rotation.y = 1.5;

    return object;
  }

}
