$(document).ready(function() {
    //To check if the gossip is complete and to auto redirect to the plots page.
    var w;

    if(typeof(Worker) !== "undefined") {
      console.log("Creating worker");
      if(typeof(w) == "undefined") {
        w = new Worker("../static/js/gossipWorker.js");
      }
      w.onmessage = function(event) {
        try {
          if($.parseJSON(event.data).inProgress == 'true') {
            console.log("The DiSC Process is in progress.");
          }
        } catch (err) {
          //Killing the worker as the gossip process is completed.
          w.terminate();
          alert("Scores have been computed. Redirecting to the Summary Tab.");
          window.location.replace("http://localhost:8000/disc/plots?redirect=true");
        }
      };
    } else {
      alert("Sorry, your browser will not support the Demo");
    }
  });
