'use strict';

class Coin3D {
    constructor(container, width, height) {
        this.scene = new THREE.Scene();
        this.width = width;
        this.height = height;

        this.camera = Coin3D.initCamera(this.scene, width, height);
        this.renderer = Coin3D.initRenderer(width, height);
        this.object = Coin3D.initObject(this.scene);

        this.animationMethod = null;

        $(container).append(this.renderer.domElement);
    }

    render() {
        const renderMethod = this.render.bind(this);
        requestAnimationFrame(renderMethod);

        if (this.animationMethod != null) this.animationMethod();

        //flipCoin();
        this.renderer.render(this.scene, this.camera);
        if (this.controls) this.controls.update();
    }

    prepareForFlip() {
        // Disable controls
        this.controls = null;
        this.animationMethod = null

        // Reset object and camera
        this.object.rotation.y = 1.5;
        this.object.rotation.x = 0;
        this.object.rotation.z = 0;
        this.camera = Coin3D.initCamera(this.scene, this.width, this.height);

    }

    enableControls() {
        this.controls = Coin3D.initControls(this.camera)
    }

    disableControls() {
        if (this.controls) {
            this.controls.dispose();
            this.controls = null;
        }
    }

    static initCamera(scene, width, height) {
        const camera = new THREE.PerspectiveCamera(70, width / height, 1, 10);
        camera.position.set(0, 3.5, 5);
        camera.lookAt(scene.position);
        return camera
    }

    static initControls(camera) {
        const controls = new THREE.TrackballControls(camera);
        controls.rotateSpeed = 5;
        controls.noZoom = true;
        controls.noPan = true;
        controls.dynamicDampingFactor = 0.02;
        return controls
    }

    static initRenderer(width, height) {
        const renderer = new THREE.WebGLRenderer({ antialias: true });
        renderer.setSize(width, height);
        renderer.shadowMap.enabled = true;
        renderer.shadowMap.type = THREE.PCFShadowMap;
        return renderer
    }

    static initObject(scene) {
        // geometry
        const diameter = 2.5;
        const radialSegments = 50;
        const geometry = new THREE.CylinderGeometry(diameter, diameter, 0.3, radialSegments);

        // materials
        const headTexture = new THREE.TextureLoader().load('./heads-transparent.png');
        headTexture.flipY = false;
        headTexture.wrapS = THREE.RepeatWrapping;
        headTexture.repeat.x = - 1;
        const materials = [
            new THREE.MeshBasicMaterial( {color: 0xEDCD71} ),
            new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load('./tails-transparent.png') }),
            new THREE.MeshBasicMaterial({ map: headTexture })
        ];

        // object
        const object = new THREE.Mesh(geometry, materials);
        object.rotation.y = 1.5;
        scene.add(object);

        //object.add(new THREE.AxisHelper(20))

        return object
    }

}