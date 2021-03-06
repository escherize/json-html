(ns json-html.core
  (:require [cheshire.core :refer :all]
            [hiccup.core :refer [html]]
            [hiccup.util :refer [escape-html]])
  (:import [clojure.lang IPersistentMap IPersistentSet IPersistentCollection Keyword]))

(defn render-keyword [k]
  (->> k ((juxt namespace name)) (remove nil?) (clojure.string/join "/")))

(defprotocol Render
  (render [this] "Renders the element a Hiccup structure"))

(extend-protocol Render
  nil
  (render [_] [:span.jh-empty nil])

  java.util.Date
  (render [this]
    [:span.jh-type-date
      (.format (java.text.SimpleDateFormat. "MMM dd, yyyy HH:mm:ss") this)])

  Character
  (render [this] [:span.jh-type-string (str this)])

  Boolean
  (render [this] [:span.jh-type-bool this])

  Integer
  (render [this] [:span.jh-type-number this])

  Double
  (render [this] [:span.jh-type-number this])

  Long
  (render [this] [:span.jh-type-number this])

  Float
  (render [this] [:span.jh-type-number this])

  Keyword
  (render [this] [:span.jh-type-string (name this)])

  String
  (render [this] [:span.jh-type-string
                  (if (.isEmpty (.trim this))
                    [:span.jh-empty-string]
                    (escape-html this))])

  IPersistentMap
  (render [this]
    (if (empty? this)
      [:div.jh-type-object [:span.jh-empty-map]]
      [:table.jh-type-object
        [:tbody
         (for [[k v] (into (sorted-map) this)]
          [:tr [:th.jh-key.jh-object-key (render k)]
               [:td.jh-value.jh-object-value (render v)]])]]))

  IPersistentSet
  (render [this]
    (if (empty? this)
      [:div.jh-type-set [:span.jh-empty-set]]
      [:ul (for [item (sort this)] [:li.jh-value (render item)])]))

  IPersistentCollection
  (render [this]
    (if (empty? this)
      [:div.jh-type-object [:span.jh-empty-collection]]
      [:table.jh-type-object
        [:tbody
          (for [[i v] (map-indexed vector this)]
             [:tr [:th.jh-key.jh-array-key i]
                  [:td.jh-value.jh-array-value (render v)]])]]))

  Object
  (render [this]
    [:span.jh-type-string (.toString this)]))


(defn edn->html [edn]
  (html
   [:div.jh-root
    (render edn)]))

(defn json->html [json]
  (-> json (parse-string false) edn->html))
