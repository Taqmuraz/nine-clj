(require
  '[nine-clj.datum :as dat]
  '[nine-clj.core :as core]
  '[nine-clj.scenes.world :as world]
  '[nine-clj.math :as math]
)
(fn [phys-world presets]
  (let [
      horse-preset (presets :horse)
      rider-presets (mapv presets [:fighter :ninja :mage :archer])
      players (mapv
        #(world/load-unit phys-world horse-preset %1 %3 [%2 2 0] [0 0 1])
        rider-presets
        (range)
	[[1 0 0 1] [0 1 0 1] [0 0 1 1] [1 1 0 1]]
      )
    ]
    players
  )
)
