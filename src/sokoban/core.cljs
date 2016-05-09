(ns ^:figwheel-always sokoban.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs.pprint :refer [pprint]]
            [cljsjs.virtual-dom]
            [cljs.core.match :refer-macros [match]]
            [sokoban.utils :refer [move completed?]]
            [sokoban.levels :refer [levels]]))

(enable-console-print!)

(defonce app-state (atom {}))
#_(add-watch app-state :key #(pprint %4))

;; Updates the status line
(defn update-status! []
  (let [{lvl-count :lvl-count num-moves :num-moves} @app-state
         stat-line (str "Level " (inc lvl-count) " - " num-moves " moves")]
    (-> (js/document.getElementById "status") .-innerHTML (set! stat-line))))

(defn elem-to-node [elem]
  (match [elem]
    ["*"] (js/virtualDom.h "div.wall")
    ["#"] (js/virtualDom.h "div.brick")
    ["@"] (js/virtualDom.h "div.brick")
    ["P"] (js/virtualDom.h "div.player")
    ["p"] (js/virtualDom.h "div.player-hole")
    ["o"] (js/virtualDom.h "div.hole")
    :else (js/virtualDom.h "div")))

;; Converts the given level into a vtree
(defn lvl-to-vtree [level] 
  (js/virtualDom.h "div#app"
    (clj->js (mapcat #(conj (mapv elem-to-node %) (js/virtualDom.h "br")) level))))

;; Mapping of key codes
(def key-codes {37 :left 38 :up 39 :right 40 :down})

;; Updates the DOM with the new state
(defn update-lvl-dom! []
  (def vtree (lvl-to-vtree (@app-state :curr-lvl)))
  (def root (->> vtree (js/virtualDom.diff (@app-state :vtree)) (js/virtualDom.patch (@app-state :root))))  
  (swap! app-state assoc :vtree vtree :root root))

;; Removes the given node from the dom
(defn remove-node! [node]
  (.removeChild (.-parentNode node) node))

;; Clears the app state
(defn clear-app-state! []
  (swap! app-state assoc :lvl-count 0 :curr-lvl nil :vtree nil :root nil) 
  (let [app-node  (js/document.getElementById "app")
        stat-node (js/document.getElementById "status")
        done-node (js/document.getElementById "done")]
    (remove-node! app-node)
    (remove-node! stat-node)
    (set! (.-className done-node) "")))

;; Goes to the next level
(defn next-level! []
  (swap! app-state update :lvl-count inc)
  (swap! app-state assoc :curr-lvl ((@app-state :curr-lvls) (@app-state :lvl-count)) :num-moves 0)
  (update-lvl-dom!)
  (update-status!))

;; Moves the player and updates the app state and virtual dom
(js/document.addEventListener "keyup"
  (fn [event]
    (let [direction (key-codes event.keyCode)]
      (when (and (some? direction) (some? (@app-state :curr-lvl)))
        (swap! app-state assoc :curr-lvl (-> (@app-state :curr-lvl) (move direction)))
        (swap! app-state update :num-moves inc)
        (update-lvl-dom!)
        (update-status!)
        (when (completed? (@app-state :curr-lvl))
          (if (= (@app-state :lvl-count) (-> :curr-lvls (@app-state) count dec))
            (clear-app-state!)
            (next-level!)))))))

;; Initializes the app state and renders the first level
(go 
  (let [curr-lvls (<! levels)
        curr-lvl  (curr-lvls 0)
        vtree     (lvl-to-vtree curr-lvl)
        root      (js/virtualDom.create vtree)]
    (remove-node! (js/document.getElementById "spinner"))
    (swap! app-state assoc :num-moves 0 :lvl-count 0 :curr-lvls curr-lvls :curr-lvl curr-lvl :vtree vtree :root root)
    (js/document.body.appendChild (@app-state :root))
    (update-status!)))

#_(defn on-js-reload [])
