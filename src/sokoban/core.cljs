(ns ^:figwheel-always sokoban.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs.pprint :refer [pprint]]
            ;[cljsjs.virtual-dom]
            [sokoban.utils :refer [move]]
            [sokoban.levels :refer [levels]]))

(enable-console-print!)

(defonce app-state (atom {}))
(go (swap! app-state assoc :levels (<! levels)))

(def level1 ((@app-state :levels) 0))
(pprint level1)
(pprint (-> level1 (move :left) (move :right) (move :right)))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
