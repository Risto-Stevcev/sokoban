(ns ^:figwheel-always sokoban.utils
  (:require [ajax.core]
            [cljs.core.match :refer-macros [match]]
            [cljs.core.async :refer [put! chan]]))

;; Provides a channel for an ajax GET request
(defn GET [url]
  (let [out (chan)
        handler #(put! out %1)]
    (ajax.core/GET url {:handler handler :error-handler handler})
    out))

;; Finds the first coordinates of the given element  
(defn find-index [matrix elem]
  (reduce
    (fn [acc curr]
      (let [index (.indexOf (to-array curr) elem)]
         (if (and (integer? acc) (= index -1))
           (inc acc)
           (if (vector? acc) acc [acc index])))) 0 matrix))

;; Checks if the player completed the level
(defn completed? [level]
  (and (-> level (find-index "o") vector? not)
       (-> level (find-index "p") vector? not)))

;; Gets the player coordinates from the given level
(defn player-coords [level]
  (let [coord (find-index level "P")]
    (if (vector? coord) coord (find-index level "p"))))

;; Get the state given the player and the next two steps in the direction s/he moves  
;; `values` (*[player move1 move2]*) - the values of the current state
(defn values-state [values]
  (match values
    ; blocked
    ["P" "*"  _ ] values

    ["P" "#" "*"] values
    ["P" "@" "*"] values

    ["P" "@" "#"] values
    ["P" "#" "@"] values
    ["P" "@" "@"] values
    ["P" "#" "#"] values

    ; move player
    ["P" " " any] [" " "P" any]
    ["p" " " any] ["o" "P" any]
    ["P" "o" any] [" " "p" any]
    ["p" "o" any] ["o" "p" any]

    ; move block
    ["P" "#" " "] [" " "P" "#"]
    ["p" "#" " "] ["o" "P" "#"]
    ["P" "#" "o"] [" " "P" "@"]
    ["p" "#" "o"] ["o" "P" "@"]

    ["P" "@" " "] [" " "p" "#"]
    ["p" "@" " "] ["o" "p" "#"]
    ["P" "@" "o"] [" " "p" "@"]
    ["p" "@" "o"] ["o" "p" "@"]

    :else values))

;; Returns the values for the state [[x y] ..] coordinates in the matrix  
;; `coords` (*[player move1 move2]*) - a 2d vector of coords  
;; returns a 1d vector of the state values
(defn coords-to-values [level coords]
  (mapv #(get-in level %) coords))

;; Updates the given level given the state coordinate-value pairs  
;; `coords-values` (*[[player-coord player-value] [move1-coord move1-value] ..]*)  
;; returns the updated level
(defn update-level [level coords-values]
  (reduce (fn [level coord-value]
            (let [[coord value] coord-value]
              (assoc-in level coord value))) level coords-values))

;; Gets the coordinate pairs for the given axis the player moves
(defn coords-move [level axis fn-move1 fn-move2]
  (let [player (player-coords level)
        move1  (update player axis fn-move1)
        move2  (update player axis fn-move2)]
    [player move1 move2]))

;; Gets the coordinate pairs if the player moves `left`
(defn coords-left [level]
  (coords-move level 1 #(- % 1) #(- % 2)))

;; Gets the coordinate pairs if the player moves `right`
(defn coords-right [level]
  (coords-move level 1 #(+ % 1) #(+ % 2)))

;; Gets the coordinate pairs if the player moves `up`
(defn coords-up [level]
  (coords-move level 0 #(- % 1) #(- % 2)))

;; Gets the coordinate pairs if the player moves `down`
(defn coords-down [level]
  (coords-move level 0 #(+ % 1) #(+ % 2)))

(def to-direction {:left coords-left :right coords-right :up coords-up :down coords-down})

;; Updates the given level based on the direction the player moves
(defn move [level direction]
  (let [coords ((to-direction direction) level)
        values (values-state (coords-to-values level coords))]
    (if (every? some? values)
      (update-level level (map vector coords values))
      level)))
