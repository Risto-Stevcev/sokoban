(ns ^:figwheel-always sokoban.levels
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [sokoban.utils :refer [GET]]
            [clojure.string :refer [split split-lines]]
            [cljs.core.async :refer [<!]]))

(def lvls-url "https://raw.githubusercontent.com/Risto-Stevcev/pysokoban/master/pysokoban/levels/")

;; Convert a string into a vector of single character strings
(defn str-to-vec [string]
  (split string #"[.]?"))

;; Converts a level string into a 2d vector
(defn lvl-to-vec [level]
  (vec (map str-to-vec (split-lines level))))

;; Fetches and returns a channel containing the levels as 2d vectors
(def levels
  (go
    (let [lvls (atom [])]
      (doseq [x [1 2 3 4 5]]
        (swap! lvls conj (lvl-to-vec (<! (GET (str lvls-url "Jordi-Domenech/Level" x ".skb"))))))
      @lvls)))
