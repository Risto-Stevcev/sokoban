(ns ^:figwheel-always sokoban.levels
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [clojure.string :refer [split split-lines]]
            [cljs.core.async :as async :refer [<!]]))

;; Convert a string into a vector of single character strings
(defn str-to-vec [string]
  (split string #"[.]?"))

;; Converts a level string into a 2d vector
(defn lvl-to-vec [level]
  (vec (map str-to-vec (split-lines level))))

;; Fetches and returns a channel containing the levels as 2d vectors
(def levels
  (let 
    [channels (mapv #(http/get (str "levels/Jordi-Domenech/Level" (inc %) ".skb")) (range 27))]
    (async/map list channels)))
