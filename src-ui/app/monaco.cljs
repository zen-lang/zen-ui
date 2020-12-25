(ns app.monaco
  (:require [app.pages :as pages]
            [reagent.core :as r]
            [stylo.core :refer [c]]
            [reagent.dom :as dom]))

(defn monaco [props]
  (let [^js/Object monacoe (when-let [m (aget js/window "monaco")] (aget m  "editor"))
        editor (atom nil)]
    (r/create-class
     {:reagent-render (fn [props attrs] [:div.monaco {:style (:style props)}])
      :component-did-mount
      (fn [this]
        (let [^js/Object e (.create ^js/Object monacoe (dom/dom-node this)
                                    #js{:language (or (:lang props) "clojure")
                                        :minimap #js{:enabled false}
                                        :value (:value props)
                                        :lineNumbers "off"
                                        :scrollbar #js{:horizontal "hidden"
                                                       :vertical "hidden"}
                                        :automaticLayout true})]
          (.onDidChangeModelContent e (fn [_] (when-let [ev (:on-change props)] (ev (.getValue e)))))
          (reset! editor e)))

      :component-will-unmount
      (fn [this]
        (when-let [^js/Object e @editor]
          (.dispose e)))

      :component-did-update
      (fn [this & args]
        (let [^js/Object model (.getModel ^js/Object @editor)
              value (:value (r/props this))
              value value
              fr (.getFullModelRange model)]
          (.pushEditOperations model #js[] #js[ #js{:range fr :text value}])))})))

#_(defn monacot [props]
  (let [editor (atom nil)]
    (r/create-class
     {:reagent-render (fn [props attrs] [:div.monaco {:style (:style props)}])

      :component-did-mount
      (fn [this]
        (let [^js/Object e (.create ^js/Object monacoe (dom/dom-node this)
                                    #js{:language "clojure"
                                        :minimap #js{:enabled false}
                                        :value (:value props)
                                        :lineNumbers "off"
                                        :scrollbar #js{:horizontal "hidden"
                                                       :vertical "hidden"}
                                        :automaticLayout true})]
          (.onDidChangeModelContent e (fn [_]
                                        (when-let [cb (:on-change props)]
                                          (cb (.getValue e)))))
          (reset! editor e)))

      :component-will-unmount
      (fn [this]
        (when-let [^js/Object e @editor]
          (.dispose e)))

      :component-did-update
      (fn [this & args]
        (let [^js/Object model (.getModel ^js/Object @editor)
              value (:value (r/props this))
              fr (.getFullModelRange model)]
          (.pushEditOperations model #js[] #js[ #js{:range fr :text value}])))})))

(defn index []
  (let [value (r/atom "{}")]
    (fn []
      [:div {:class (c [:p 10])}
       [:h1 "Monaco"]
       [:style ".monaco {width: 500px; min-height: 200px; border: 1px solid #ddd;}"]
       [:button {:on-click #(reset! value "key2: val2\n")} "Change value"]
       [:pre (pr-str @value)]
       [monaco {:value @value :on-change #(fn [x] (println x))}]])))

(pages/reg-page :monaco/index #'index)
