(ns nine-clj.input
  [:require
    [nine-clj.math :as math]
  ]
  [:import
    [nine.lwjgl
      LWJGL_Keyboard
      LWJGL_Mouse
    ]
    [nine.math
      Vector2f
    ]
  ]
)

(defn mouse [wid status]
  (let [
      m (LWJGL_Mouse. wid status)
    ]
    (fn [key]
      (case key
        :delta (math/floats-from-vec2f(.delta m))
        :pos (math/floats-from-vec2f(.position m))
        :update ()
        :left-down (-> m .left .isDown)
        :right-down (-> m .right .isDown)
        :middle-down (-> m .middle .isDown)
      )
    )
  )
)

(defn viewport-mouse [mouse width-fun height-fun]
  (fn [key]
    (let [
        w (width-fun)
        h (height-fun)
        [mx my] (mouse :pos)
        my (- h my)
      ]
      (case key
        :pos (->>
          [w h]
          (mapv / [mx my])
          (mapv + [-0.5 -0.5])
          (mapv * [2 2])
        )
        :delta (mapv / (mouse :delta) [(width-fun) (height-fun)])
        (mouse key)
      )
    )
  )
)

(defn keyboard-update [dev](.update dev))

(defn keyboard [wid]
  (LWJGL_Keyboard. wid)
)

(defn key-down [keyboard key] (-> keyboard (.keyOf key) .isDown))
(defn key-up [keyboard key] (-> keyboard (.keyOf key) .isUp))
(defn escape-down [keyboard] (-> keyboard .escape .isDown))

(defn wasd [kb]
  (mapv +
    (if (key-down kb \w) [0 1] [0 0])
    (if (key-down kb \a) [-1 0] [0 0])
    (if (key-down kb \s) [0 -1] [0 0])
    (if (key-down kb \d) [1 0] [0 0])
  )
)