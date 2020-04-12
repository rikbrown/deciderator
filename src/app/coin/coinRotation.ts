import {ElementRef, HostListener} from '@angular/core';
import * as THREE from 'three';
import {MathUtils} from 'three';
import clamp = MathUtils.clamp;
import {CoinState} from '../core/services/coin/coin.service';
import {BehaviorSubject, Observable} from 'rxjs';

export class CoinRotation {
  private rendererContainer: ElementRef;

  private isMouseDown = false;

  private rotateStartPoint = new THREE.Vector3(0, 0, 1);
  private rotateEndPoint = new THREE.Vector3(0, 0, 1);
  private startPoint = {
    x: 0,
    y: 0,
  };
  private lastMoveTimestamp: Date = new Date();
  private moveReleaseTimeDelta = 50;

  private deltaX = 0;
  private deltaY = 0;
  private rotationSpeed = 2;
  private drag = 0.95;

  private object: THREE.Mesh;

  private coinState: BehaviorSubject<CoinState>;

  constructor(object: THREE.Mesh, rendererContainer: ElementRef) {
    this.object = object;
    this.rendererContainer = rendererContainer;
    this.coinState = new BehaviorSubject(this.toCoinState());
  }

  observeCoinState(): Observable<CoinState> {
    return this.coinState.asObservable();
  }

  updateCoinState(coinState: CoinState): void {
    console.log('updating coin state', coinState);

    if (coinState.rotateDelta) {
      this.deltaX = coinState.rotateDelta.x;
      this.deltaY = coinState.rotateDelta.y;
    }
    if (coinState.quaternion) {
      this.object.quaternion.set(
        coinState.quaternion.x,
        coinState.quaternion.y,
        coinState.quaternion.z,
        coinState.quaternion.w
      );
    }
    this.drag = coinState.drag;
    this.rotationSpeed = coinState.rotationSpeed;
  }

  onMouseDown(event) {
    event.preventDefault();

    this.isMouseDown = true;

    this.startPoint = {
      x: event.clientX,
      y: event.clientY
    };

    this.rotateStartPoint = this.rotateEndPoint = this.projectOnTrackball(0, 0);
  }

  onMouseMove(event) {
    if (!this.isMouseDown) {
      return;
    }

    this.deltaX = event.x - this.startPoint.x;
    this.deltaY = event.y - this.startPoint.y;

    this.handleRotation();

    this.startPoint.x = event.x;
    this.startPoint.y = event.y;

    this.lastMoveTimestamp = new Date();
  }

  onMouseUp(event): [number, number] {
    if (!this.isMouseDown) {
      return [null, null];
    }

    if (new Date().getTime() - this.lastMoveTimestamp.getTime() > this.moveReleaseTimeDelta) {
      this.deltaX = event.x - this.startPoint.x;
      this.deltaY = event.y - this.startPoint.y;
    }

    this.isMouseDown = false;

    return [this.deltaX, this.deltaY];
  }

  handleRotation() {
    const minDelta = 0.05;

    if (this.deltaX < -minDelta || this.deltaX > minDelta) {
      this.deltaX *= this.drag;
    } else {
      this.deltaX = 0;
    }

    if (this.deltaY < -minDelta || this.deltaY > minDelta) {
      this.deltaY *= this.drag;
    } else {
      this.deltaY = 0;
    }

    this.rotateEndPoint = this.projectOnTrackball(this.deltaX, this.deltaY);

    const rotateQuaternion = this.rotateMatrix(this.rotateStartPoint, this.rotateEndPoint);
    const curQuaternion = this.object.quaternion;
    curQuaternion.multiplyQuaternions(rotateQuaternion, curQuaternion);
    curQuaternion.normalize();
    this.object.setRotationFromQuaternion(curQuaternion);

    this.rotateEndPoint = this.rotateStartPoint;

    // Emit event
    this.coinState.next(this.toCoinState());
  }

  private projectOnTrackball(touchX: number, touchY: number): THREE.Vector3 {
    const mouseOnBall = new THREE.Vector3();

    const xHalf = this.rendererContainer.nativeElement.offsetWidth / 2;
    const yHalf = this.rendererContainer.nativeElement.offsetHeight / 2;

    mouseOnBall.set(
      clamp(touchX / xHalf, -1, 1), clamp(-touchY / yHalf, -1, 1),
      0.0
    );

    const length = mouseOnBall.length();

    if (length > 1.0) {
      mouseOnBall.normalize();
    } else {
      mouseOnBall.z = Math.sqrt(1.0 - length * length);
    }

    return mouseOnBall;
  }

  private rotateMatrix(rotateStart: THREE.Vector3, rotateEnd: THREE.Vector3): THREE.Quaternion {
    const axis = new THREE.Vector3();
    const quaternion = new THREE.Quaternion();

    let angle = Math.acos(rotateStart.dot(rotateEnd) / rotateStart.length() / rotateEnd.length());

    if (angle) {
      axis.crossVectors(rotateStart, rotateEnd).normalize();
      angle *= this.rotationSpeed;
      quaternion.setFromAxisAngle(axis, angle);
    }

    return quaternion;
  }

  private toCoinState(): CoinState {
    return {
      interactive: true, // FIXME
      rotateDelta: {
        x: this.deltaX,
        y: this.deltaY,
      },
      rotationSpeed: this.rotationSpeed,
      drag: this.drag,
      quaternion: {
        w: this.object.quaternion.w,
        x: this.object.quaternion.x,
        y: this.object.quaternion.y,
        z: this.object.quaternion.z,
      }
    };
  }



}
