$(document).ready(function() {
    //To check if the gossip is complete and to auto redirect to the plots page.
    var w;
    //Note worker thread needs to be stopped after the gossip process is complete.
    if(typeof(Worker) !== "undefined") {
      console.log("Creating worker");
      if(typeof(w) == "undefined") {
        w = new Worker("../static/js/gossipWorker.js");
      }
      w.onmessage = function(event) {
        try {
          if($.parseJSON(event.data).inProgress == 'true') {
            console.log("The Gossip Process is in progress.");
          }
        } catch (err) {
          //Killing the worker as the gossip process is completed.
          w.terminate();
          alert("The Score has been generated. Redirecting to the Summary tab.")
          window.location.replace("http://localhost:8000/disc/plots?redirect=true");
        }
      };
    } else {
      alert("Sorry, your browser will not support the Demo");
    }
  });
