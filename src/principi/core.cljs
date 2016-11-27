(ns principi.core
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :as async  :refer (<! >! put! chan)]
            [cljs.pprint :as pp]
            [taoensso.encore :as encore :refer-macros (have have?)]
            [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
            [taoensso.sente  :as sente  :refer (cb-success?)])
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)]))

(enable-console-print!)
(println "Edits to this text should show up in your developer console.")

(def app-state (atom {:broadcasts []}))

;;;; Util for logging output to on-screen console
;; (def output-el (.getElementById js/document "output"))
(defn ->output! [fmt & args]
  ;; nil
  (let [msg (apply encore/format fmt args)]
    (timbre/debug msg)
    ;; (aset output-el "value" (str "• " (.-value output-el) "\n" msg))
    ;; (aset output-el "scrollTop" (.-scrollHeight output-el))
    )
  )

;; client
(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/ws" ; Note the same path as before
                                  {:type :auto ; e/o #{:auto :ajax :ws}
                                   })]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

;;;; Sente event handlers

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id ; Dispatch on event-id
  )

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (->output! "Unhandled event: %s" event))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (->output! "Channel socket successfully established!: %s" new-state-map)
      (->output! "Channel socket state change: %s"              new-state-map))))

(defmulti -recv-handler first)

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (when ?data
    (-recv-handler ?data))
  (->output! "Push event from server: %s" ?data))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (->output! "Handshake: %s" ?data)))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-client-chsk-router!
           ch-chsk event-msg-handler)))

(defn start! [] (start-router!))
(defonce _start-once (start!))

(defmethod -recv-handler :some/broadcast
  [[_ {:as data :keys [what-is-this]}]]
  (->output! "%s: %s" what-is-this data)
  (swap! app-state update-in [:broadcasts] conj data))

(defn toggle-broadcast []
  (chsk-send! [:example/toggle-broadcast] 5000
               (fn [cb-reply] (->output! "Callback reply: %s" cb-reply))))

(defn hello-world []
  [:div
   [:button {:on-click toggle-broadcast} "Toggle broadcast"]
   [:ul (for [b (@app-state :broadcasts)] ^{:key b} [:li (prn-str b)])]])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc))
