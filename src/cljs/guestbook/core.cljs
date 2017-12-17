(ns guestbook.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST]]))

(defn message-list [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p  message]
      [:p [:font {:size 2} " - " name]]])])

(defn get-messages [messages]
  (GET "/messages"
       {:headers {"Accept" "application/transit+json"}
        :handler #(reset! messages (vec %))}))

(defn send-message! [fields errors messages]
  (POST "/add-message"
        {:headers
         {"Accept" "application/transit+json"
          "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :params @fields
         :handler #(do
                     (reset! errors nil)
                     (swap! messages conj (assoc @fields :timestamp (js/Date.))))
         :error-handler #(do
                           (.log js/console (str "error:" %))
                           (reset! errors (get-in % [:response :errors])))}))

(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.alert.alert-danger (clojure.string/join error)]))

(defn message-form [messages]
  (let [fields (atom {})
        errors (atom nil)]
    (fn []
      "
<div class=\"content\">
 <div class=\"form-group\">
  <p> name: (:name @fields)
   <p> message: (:message @fields)</p>
  </p>
  <p> Name:
    <input class=\"form-control\"
           type=text
           name=name
           on-change=#(swap! fields assoc :name (-> % .-target .-value))>
           value=(:name @fields)
    </input>
  </p>
  <p> Message:
    <textarea class=\"form-control\"
           rows=4
           col=50
           name=message
           on-change=#(swap! fields assoc :message (-> % .-target .-value))>
      (:message @fields)
    </textarea>
  </p>
  <input class=\"btn\"
         class=\"btn-primary\"
         type=submit
         on-click=#(send-message! fields)
         value=\"comment\">
  </input>
 </div>
</div>"
      [:div.content
       [:div.form-group
        [errors-component errors :name]
        [:p "Name:"
         [:input.form-control
          {:type :text
           :name :name
           :on-change #(swap! fields assoc :name (-> % .-target .-value))
           :value (:name @fields)}]]
        [errors-component errors :message]
        [:p "Message:"
         [:textarea.form-control
          {:rows 4
           :cols 50
           :name :message
           :on-change #(swap! fields assoc :message (-> % .-target .-value))}
          (:message @fields)]]
        [:input.btn.btn-primary {:type :submit
                                 :on-click #(send-message! fields errors messages)
                                 :value "comment"}]]])))

(defn home []
  (let [messages (atom nil)]
    (get-messages messages)
    (fn []
      [:div
       [:div.row
        [:div.span12
         [message-list messages]]]
       [:div.span12
        [message-form messages]]
       ])))

(reagent/render
 [home]
 (.getElementById js/document "content"))
