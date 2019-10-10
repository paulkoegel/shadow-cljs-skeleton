(ns shadow-cljs-skeleton.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-http.client :as http]))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:posts []
                          :index 0
                          :text "Hello world!"}))

(def posts-cursor (reagent/cursor app-state [:posts]))
(def index-cursor (reagent/cursor app-state [:index]))

(defn full-screen-posts []
  [:div
   [:div.preloaded-images {:width 0
                           :height 0
                           :display "inline"}
    (map
      (fn [post-url]
        [:div {:style {:background-image (str "url(" post-url ")")}
               :key post-url}])
      @posts-cursor)]

   [:div.counter {:style {:top 0
                          :left 0
                          :position "absolute"
                          :background "black"
                          :color "white"
                          :font-size 20}}
    @index-cursor]
   [:img {:style {:background-image (str "url(" (get @posts-cursor @index-cursor) ")")
                  :background-size "cover"
                  :width "100vw"
                  :height "100vh"}
          :onClick (fn []
                     (swap! app-state (fn [state]
                                        (update state :index (fn [index] (mod (inc index) (count (:posts state))))))))}]])

(defn start []
  (reagent/render-component [full-screen-posts]
                            (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))

(go (let [response (<! (http/get "https://www.juicer.io/api/feeds/bonjovi"
                                 {:query-params {:page 1
                                                 :per 20}
                                  :with-credentials? false}))
          posts (-> response
                    :body
                    :posts
                    :items)]
      (swap! app-state (fn [state] (assoc state :posts (mapv :image posts))))))

(comment
  )
