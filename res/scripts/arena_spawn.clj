(require
  '[nine-clj.datum :as dat]
  '[nine-clj.core :as core]
)
(fn [phys-world presets]
  (let [
      make-char (fn [preset pos dir color side] (dat/load-char phys-world preset pos dir color side core/get-time))
      step (fn [n] (* (quot (inc n) 2) (Math/pow -1 (rem n 2))))
      step (comp (partial * 2) step)
      make-red (fn [preset n] (make-char preset [(- 0 25 (* 2 (quot n 15))) 0 (step (mod n 10))] [1 0 0] [1 0 0 1] :red))
      make-green (fn [preset n] (make-char preset [(+ 25 (* 2 (quot n 15))) 0 (step (mod n 10))] [-1 0 0] [0 1 0 1] :green))
      players (concat
        (map make-green (repeat (presets :archer)) (range 1))
        (map make-red (repeat (presets :archer)) (range 1))
      )
    ]
    players
  )
)