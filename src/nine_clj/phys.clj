(ns nine-clj.phys
  (:require
    [nine-clj.geom :as geom]
  )
  (:import
    (com.bulletphysics.dynamics
      DiscreteDynamicsWorld
      RigidBody
      RigidBodyConstructionInfo
    )
    (com.bulletphysics.dynamics.constraintsolver
      SequentialImpulseConstraintSolver
    )
    (com.bulletphysics.collision.broadphase
      AxisSweep3
    )
    (com.bulletphysics.collision.dispatch
      CollisionDispatcher
    )
    (com.bulletphysics.collision.dispatch
      DefaultCollisionConfiguration
    )
    (com.bulletphysics.linearmath
      Transform
      DefaultMotionState
    )
    (com.bulletphysics.collision.shapes
      BoxShape
      CapsuleShape
      SphereShape
      StaticPlaneShape
      BvhTriangleMeshShape
      TriangleIndexVertexArray
    )
    (javax.vecmath
      Vector3f
      Matrix4f
      Matrix3f
    )
    (java.nio
      ByteBuffer
      ByteOrder
    )
  )
)

(defn dynamics-world []
  (let [
      collision-config (DefaultCollisionConfiguration.)
      dispatcher (CollisionDispatcher. collision-config)
      broadphase (AxisSweep3. (Vector3f. -10000 -10000 -10000) (Vector3f. 10000 10000 10000))
      solver (SequentialImpulseConstraintSolver.)
      dynamics-world (DiscreteDynamicsWorld. dispatcher broadphase solver collision-config)
    ]
    (.setGravity dynamics-world (Vector3f. 0 -9.81 0))
    dynamics-world
  )
)

(defn add-rigid-body [dynamics-world shape pos rot mass]
  (let [
      [px py pz] pos
      [rx ry rz] rot
      mat3 (Matrix3f.)
      mat3 (do
        (.setIdentity mat3)
        (.rotX mat3 rx)
        (.rotY mat3 ry)
        (.rotZ mat3 rz)
        mat3
      )
      mat4 (Matrix4f. mat3 (Vector3f. px py pz) (float 1))
      transform (Transform. mat4)
      motion-state (DefaultMotionState. transform)
      local-inertia (Vector3f. 0 0 0)
    ]
    (.calculateLocalInertia shape mass local-inertia)
    (let [
        rbci (RigidBodyConstructionInfo. mass motion-state shape local-inertia)
        body (RigidBody. rbci)
      ]
      (.addRigidBody dynamics-world body)
      body
    )
  )
)

(defn box [world pos rot [scale-x scale-y scale-z] mass]
  (add-rigid-body world (BoxShape. (Vector3f. scale-x scale-y scale-z))
    pos rot mass
  )
)

(defn capsule [world pos rot radius height mass]
  (add-rigid-body world (CapsuleShape. radius height)
    pos rot mass
  )
)

(defn sphere [world pos rot radius mass]
  (add-rigid-body world (SphereShape. radius)
    pos rot mass
  )
)

(defn plane[world [nx ny nz] const]
  (add-rigid-body world
    (StaticPlaneShape. (Vector3f. nx ny nz) const)
    [0 0 0] [0 0 0] 0
  )
)

(defn set-velocity [body [vx vy vz]]
  (.activate body)
  (.setLinearVelocity body (Vector3f. vx vy vz))
  body
)

(defn get-velocity [body]
  (let [
      v (.getLinearVelocity body (Vector3f.))
    ]
    [(.x v) (.y v) (.z v)]
  )
)

(defn set-angular-velocity [body [vx vy vz]]
  (.activate body)
  (.setAngularVelocity body (Vector3f. vx vy vz))
  body
)

(defn get-angular-velocity [body]
  (let [
      v (.getAngularVelocity body (Vector3f.))
    ]
    [(.x v) (.y v) (.z v)]
  )
)

(defn set-matrix [body mat]
  (let [
      m (Matrix4f.)
      cs (partition 4 (map float mat))
      m (doseq [[i [x y z w]] (map vector (range) cs)]
        (.setColumn m i x y z w)
      )
      t (Transform. m)
    ]
    (.setCenterOfMassTransform body t)
  )
  body
)

(defn set-rotation-enabled [body v]
  (.setAngularFactor body (if v 1 0))
  body
)

(defn set-position [body [x y z]]
  (let [
      t (.getCenterOfMassTransform body (Transform.))
      m (.getMatrix t (Matrix4f.))
      m (do (.setColumn 3 x y z 1) m)
      t (Transform. m)
    ]
    (.setCenterOfMassTransform body t)
  )
  body
)

(defn get-matrix [body]
  (let [
      t (.getCenterOfMassTransform body (Transform.))
      m (.getMatrix t (Matrix4f.))
      cs (mapv (partial make-array Float/TYPE) [4 4 4 4])
    ]
    (doseq [i (range 4)] (.getColumn m (int i) (cs i)))
    ((comp vec (partial apply concat)) cs)
  )
)

(defn get-position [body]
  (let [
      t (.getCenterOfMassTransform body (Transform.))
      m (.getMatrix t (Matrix4f.))
      fs (make-array Float/TYPE 4)
    ]
    (.getColumn m 3 fs)
    (vec (take 3 fs))
  )
)

(defn update-world [world time-step]
  (.stepSimulation world time-step)
  world
)

(defn byte-buffer [size]
  (.order (. ByteBuffer allocateDirect size) (. ByteOrder nativeOrder))
)

(defn float-byte-buffer [fs]
  (let [
      v (vec fs)
      c (count v)
      b (byte-buffer (* 4 c))
    ]
    (doseq [i (range c)]
      (.putFloat b (* 4 i) (float (v i)))
    )
    b
  )
)

(defn int-byte-buffer [is]
  (let [
      v (vec is)
      c (count v)
      b (byte-buffer (* 4 c))
    ]
    (doseq [i (range c)]
      (.putInt b (* 4 i) (int (v i)))
    )
    b
  )
)

(defn geom-shape [verts]
  (let [
      v (vec verts)
      c (/ (count v) 3)
      i (vec (range c))
      t (/ (count i) 3)
      vbuf (float-byte-buffer v)
      ibuf (int-byte-buffer i)
      arr (TriangleIndexVertexArray. t ibuf 12 c vbuf 12)
    ]
    (BvhTriangleMeshShape. arr true)
  )
)

(defn move-char [body vel]
  (let [
      [vx vy vz] (get-velocity body)
      [mx my mz] vel
    ]
    (set-velocity body [mx vy mz])
  )
)