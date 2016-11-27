(defproject principi "0.1.0-SNAPSHOT"
  :description "Clojure/ClojureScript template for Heroku"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojure/core.async "0.2.391"]
                 [org.clojure/tools.nrepl   "0.2.12"] ; Optional, for Cider

                 [reagent "0.5.1"]
                 [compojure "1.5.1"]
                 [environ "1.0.0"]

                 [http-kit "2.2.0"]
                 [ring                      "1.5.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-defaults        "0.2.1"] ; Includes `ring-anti-forgery`, etc.

                 [com.taoensso/sente        "1.11.0-RC1"] ; <--- Sente
                 [com.taoensso/timbre       "4.7.4"]]

  :plugins [[lein-pprint         "1.1.2"]
            [lein-ancient        "0.6.10"]
            [com.cemerick/austin "0.1.6"]
            [environ/environ.lein "0.3.1"]
            ;; [cider/cider-nrepl "0.12.0-snapshot"]
            [lein-figwheel "0.5.2"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]]

  :hooks [environ.leiningen.hooks]

  :uberjar-name "principi-standalone.jar"
  :profiles {:production {:env {:production true}}}

  :prep-tasks ["compile" ["cljsbuild" "once" "production"]]

  :source-paths ["src"]
  :resource-paths ["resources"]

  :uberjar {:source-paths ["src"]
            ;; :hooks [leiningen.cljsbuild]
            :omit-source true
            :aot :all
            :main principi.web}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [
               {:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "principi.core/on-js-reload"}
                :compiler {:main principi.core
                           :output-to "resources/public/js/compiled/principi.js"
                           :asset-path "js/compiled/out-dev"
                           :output-dir "resources/public/js/compiled/out-dev"
                           :source-map-timestamp true
                           }}

               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               {:id "production"
                :jar true
                :source-paths ["src"]
                :compiler {:main principi.core
                           :output-to "resources/public/js/compiled/principi.js"
                           :asset-path "js/compiled/out"
                           :output-dir "resources/public/js/compiled/out"
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             :server-port 5000 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             :ring-handler principi.web/app-handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log"
             })
