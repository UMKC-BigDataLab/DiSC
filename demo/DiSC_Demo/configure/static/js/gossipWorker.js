var output;

var ajax = function(url, type) {
  output = "";
  var req = new XMLHttpRequest();
  req.open(type, url, false);
  req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  req.onreadystatechange = function() {
    if (req.readyState === 4 && req.status === 200) {
      output = req.responseText
    }
  };
  req.send("");
  return req;
};

function timedCount() {
  ajax('http://localhost:8000/disc/plots?redirect=false','GET');
  self.postMessage(output);
  setTimeout("timedCount()",50000);
}

timedCount();
