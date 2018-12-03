//IP List
var IP1 = "128.110.154.247";
var IP2 = "128.110.154.248";
var IP3 = "128.110.154.250";
var IP4 = "128.110.154.242";
var IP5 = "128.110.155.11";
var IP6 = "128.110.155.14";
var IP7 = "128.110.155.25";
var IP8 = "128.110.155.29";
var IP9 = "128.110.155.9";
var IP10 = "128.110.155.5";
var IP11 = "128.110.154.244";
var IP12 = "128.110.155.13";
var IP13 = "128.110.155.4";
var IP14 = "128.110.154.246";
var IP15 = "128.110.154.253";
var IP16 = "128.110.155.8";

var node1Count = 0;
var node2Count = 0;
var node3Count = 0;
var node4Count = 0;
var node5Count = 0;
var node6Count = 0;
var node7Count = 0;
var node8Count = 0;
var node9Count = 0;
var node10Count = 0;
var node11Count = 0;
var node12Count = 0;
var node13Count = 0;
var node14Count = 0;
var node15Count = 0;
var node16Count = 0;

$(document).ready(function(){

  var nodes=document.getElementById('NodesCount').value;
  console.log("nodes :: " + nodes);

  var j;
  for (j = 1;j <= nodes; j++) {
    //Node Num
    var iDiv = document.createElement('div');
    iDiv.id = 'field'+j;
    iDiv.className = 'field';
    document.getElementById("container").appendChild(iDiv);
    var textnode = document.createTextNode(j);
    document.getElementById('field'+j).appendChild(textnode);

    //Counter
    var jDiv = document.createElement('div');
    jDiv.id = 'count'+j;
    jDiv.className = 'count';
    document.getElementById("container").appendChild(jDiv);
    var textnode = document.createTextNode("Msg Rcvd: 0");
    document.getElementById('count'+j).appendChild(textnode);
  }

  function makeSVG(tag, attrs) {
    var el= document.createElementNS('http://www.w3.org/2000/svg', tag);
    for (var k in attrs)
    el.setAttribute(k, attrs[k]);
    return el;
  }

  var map = new Object();
  var radius = 180;
  var fields = $('.field'), container = $('#container'), width = container.width(), height = container.height();
  var angle = 0, step = (2*Math.PI) / fields.length;
  var i=1;
  fields.each(function() {
    var x = Math.round(width/2 + radius * Math.cos(angle) - $(this).width()/2);
    var y = Math.round(height/2 + radius * Math.sin(angle) - $(this).height()/2);
    var cordMap = {"x":x,"y":y};
    map[i] = cordMap;
    $(this).css({
      left: x + 'px',
      top: y + 'px'
    });
    angle += step;
    i += 1;
  });


  var countMap = new Object();
  var countRadius = 250;
  var countfields = $('.count'), container = $('#container'), width = container.width(), height = container.height();
  var angle = 0, step = (2*Math.PI) / fields.length;
  var i=1;
  countfields.each(function() {
    var x = Math.round(width/2 + countRadius * Math.cos(angle) - $(this).width()/2);
    var y = Math.round(height/2 + countRadius * Math.sin(angle) - $(this).height()/2);
    var cordMap = {"x":x,"y":y};
    countMap[i] = cordMap;
    $(this).css({
      left: x + 'px',
      top: y + 'px'
    });
    angle += step;
    i += 1;
  });


  for (var key1 in map) {
    for(var key2 in map) {
      if(key1 != key2) {
        var srcMap = map[key1];
        var destMap = map[key2];
        var xa = srcMap["x"];
        var ya = srcMap["y"];
        var xb = destMap["x"];
        var yb = destMap["y"];
        //console.log("x1 :: " + xa + " :: y1 :: " + ya);
        //console.log("x2 :: " + xb + " :: y2 :: " + yb);
        var line= makeSVG('line', {id: key1+"-"+key2, display: "none",x1: xa+20, y1: ya+20, x2: xb+20, y2:yb+20, stroke: 'black', 'stroke-width': 2, 'marker-end':"url(#arrow)"});
        document.getElementById("svg").appendChild(line);

        //Adding the arrow heads
        var arr1 = makeSVG('animate', {id: key1+"-"+key2, display: "none",attributeType:"XML",attributeName:"x2",from:xa+20,to:xb+20,dur:"1.02s", repeatCount:"indefinite"});
        line.appendChild(arr1);
        var arr2 = makeSVG('animate', {id: key1+"-"+key2, display: "none",attributeType:"XML",attributeName:"y2",from:ya+20,to:yb+20,dur:"1.02", repeatCount:"indefinite"});
        line.appendChild(arr2);
      }
    }
  }
  //triggerNode1("2");
  //triggerNode8("16");
  //setTimeout(function() {
    //triggerNode1("2");
  //}, 3000);

});

function triggerNode1(eventData) {
  console.log("Received the WS 1 data :: " + eventData);

  if(eventData == '2') {
    $("#1-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#1-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#1-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);

  }

  if(eventData == '5') {
    $("#1-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#1-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#1-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#1-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#1-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#1-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#1-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#1-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#1-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#1-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#1-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#1-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#1-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode2(eventData) {
  console.log("Received the WS 2 data :: " + eventData);

  if(eventData == '1') {
    $("#2-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '3') {
    $("#2-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#2-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#2-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#2-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#2-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#2-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#2-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#2-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#2-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#2-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#2-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#2-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#2-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#2-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#2-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode3(eventData) {
  console.log("Received the WS 3 data :: " + eventData);

  if(eventData == '1') {
    $("#3-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#3-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '4') {
    $("#3-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#3-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);
  }

  if(eventData == '6') {
    $("#3-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#3-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#3-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#3-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#3-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#3-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#3-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#3-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#3-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#3-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#3-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#3-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode4(eventData) {
  console.log("Received the WS 4 data :: " + eventData);

  if(eventData == '1') {
    $("#4-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#4-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#4-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '5') {
    $("#4-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#4-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#4-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#4-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#4-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#4-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#4-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#4-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#4-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#4-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#4-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#4-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#4-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode5(eventData) {
  console.log("Received the WS 5 data :: " + eventData);

  if(eventData == '1') {
    $("#5-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#5-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#5-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#5-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '6') {
    $("#5-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#5-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#5-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#5-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#5-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#5-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#5-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#5-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#5-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#5-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#5-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#5-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode6(eventData) {
  console.log("Received the WS 6 data :: " + eventData);

  if(eventData == '1') {
    $("#6-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#6-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#6-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#6-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#6-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '7') {
    $("#6-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#6-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#6-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#6-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#6-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#6-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#6-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#6-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#6-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#6-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#6-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode7(eventData) {
  console.log("Received the WS 7 data :: " + eventData);

  if(eventData == '1') {
    $("#7-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#7-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#7-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#7-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#7-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#7-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '8') {
    $("#7-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#7-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#7-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#7-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#7-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#7-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#7-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#7-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#7-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#7-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode8(eventData) {
  console.log("Received the WS 8 data :: " + eventData);

  if(eventData == '1') {
    $("#8-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#8-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#8-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#8-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#8-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#8-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#8-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '9') {
    $("#8-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#8-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#8-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#8-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#8-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#8-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#8-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#8-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#8-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode9(eventData) {
  console.log("Received the WS 9 data :: " + eventData);

  if(eventData == '1') {
    $("#9-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#9-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#9-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#9-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#9-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#9-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#9-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#9-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '10') {
    $("#9-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#9-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#9-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#9-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#9-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#9-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#9-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#9-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode10(eventData) {
  console.log("Received the WS 10 data :: " + eventData);

  if(eventData == '1') {
    $("#10-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#10-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#10-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#10-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#10-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#10-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#10-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#10-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#10-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '11') {
    $("#10-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#10-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#10-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#10-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#10-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#10-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#10-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode11(eventData) {
  console.log("Received the WS 11 data :: " + eventData);

  if(eventData == '1') {
    $("#11-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#11-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#11-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#11-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#11-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#11-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#11-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#11-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#11-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#11-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '12') {
    $("#11-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#11-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#11-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#11-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#11-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#11-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode12(eventData) {
  console.log("Received the WS 12 data :: " + eventData);

  if(eventData == '1') {
    $("#12-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#12-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#12-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#12-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#12-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#12-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#12-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#12-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#12-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#12-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#12-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '13') {
    $("#12-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#12-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#12-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#12-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#12-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode13(eventData) {
  console.log("Received the WS 13 data :: " + eventData);

  if(eventData == '1') {
    $("#13-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#13-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#13-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#13-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#13-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#13-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#13-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#13-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#13-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#13-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#13-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#13-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '14') {
    $("#13-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#13-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#13-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#13-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode14(eventData) {
  console.log("Received the WS 14 data :: " + eventData);

  if(eventData == '1') {
    $("#14-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#14-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#14-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#14-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#14-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#14-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#14-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#14-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#14-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#14-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#14-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#14-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#14-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '15') {
    $("#14-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

  if(eventData == '16') {
    $("#14-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#14-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode15(eventData) {
  console.log("Received the WS 15 data :: " + eventData);

  if(eventData == '1') {
    $("#15-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#15-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#15-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#15-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#15-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#15-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#15-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#15-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#15-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#15-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#15-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#15-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#15-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#15-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '16') {
    $("#15-16").removeAttr("display", "none");

    setTimeout(function() {
      $("#15-16").attr("display", "none");
    }, 1000);

    node16Count += 1;
    $("#count16").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node16Count);
    $("#count16").append(textnode);

  }
}

function triggerNode16(eventData) {
  console.log("Received the WS 16 data :: " + eventData);

  if(eventData == '1') {
    $("#16-1").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-1").attr("display", "none");
    }, 1000);

    node1Count += 1;
    $("#count1").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node1Count);
    $("#count1").append(textnode);

  }

  if(eventData == '2') {
    $("#16-2").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-2").attr("display", "none");
    }, 1000);

    node2Count += 1;
    $("#count2").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node2Count);
    $("#count2").append(textnode);

  }

  if(eventData == '3') {
    $("#16-3").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-3").attr("display", "none");
    }, 1000);

    node3Count += 1;
    $("#count3").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node3Count);
    $("#count3").append(textnode);

  }

  if(eventData == '4') {
    $("#16-4").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-4").attr("display", "none");
    }, 1000);

    node4Count += 1;
    $("#count4").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node4Count);
    $("#count4").append(textnode);


  }

  if(eventData == '5') {
    $("#16-5").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-5").attr("display", "none");
    }, 1000);

    node5Count += 1;
    $("#count5").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node5Count);
    $("#count5").append(textnode);

  }

  if(eventData == '6') {
    $("#16-6").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-6").attr("display", "none");
    }, 1000);

    node6Count += 1;
    $("#count6").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node6Count);
    $("#count6").append(textnode);

  }

  if(eventData == '7') {
    $("#16-7").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-7").attr("display", "none");
    }, 1000);

    node7Count += 1;
    $("#count7").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node7Count);
    $("#count7").append(textnode);

  }

  if(eventData == '8') {
    $("#16-8").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-8").attr("display", "none");
    }, 1000);

    node8Count += 1;
    $("#count8").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node8Count);
    $("#count8").append(textnode);

  }

  if(eventData == '9') {
    $("#16-9").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-9").attr("display", "none");
    }, 1000);

    node9Count += 1;
    $("#count9").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node9Count);
    $("#count9").append(textnode);

  }

  if(eventData == '10') {
    $("#16-10").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-10").attr("display", "none");
    }, 1000);

    node10Count += 1;
    $("#count10").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node10Count);
    $("#count10").append(textnode);

  }

  if(eventData == '11') {
    $("#16-11").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-11").attr("display", "none");
    }, 1000);

    node11Count += 1;
    $("#count11").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node11Count);
    $("#count11").append(textnode);

  }

  if(eventData == '12') {
    $("#16-12").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-12").attr("display", "none");
    }, 1000);

    node12Count += 1;
    $("#count12").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node12Count);
    $("#count12").append(textnode);

  }

  if(eventData == '13') {
    $("#16-13").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-13").attr("display", "none");
    }, 1000);

    node13Count += 1;
    $("#count13").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node13Count);
    $("#count13").append(textnode);

  }

  if(eventData == '14') {
    $("#16-14").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-14").attr("display", "none");
    }, 1000);

    node14Count += 1;
    $("#count14").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node14Count);
    $("#count14").append(textnode);

  }

  if(eventData == '15') {
    $("#16-15").removeAttr("display", "none");

    setTimeout(function() {
      $("#16-15").attr("display", "none");
    }, 1000);

    node15Count += 1;
    $("#count15").empty();
    var textnode = document.createTextNode("Msg Rcvd: " + node15Count);
    $("#count15").append(textnode);


  }

}
