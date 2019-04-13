$(document).ready(function() {

  //IP List
  var IP1 = "128.110.152.64";
  var IP2 = "128.110.152.85";
  var IP3 = "128.110.152.82";
  var IP4 = "128.110.152.66";
  var IP5 = "128.110.152.83";
  var IP6 = "128.110.152.88";
  var IP7 = "128.110.152.56";
  var IP8 = "128.110.152.71";
  var IP9 = "128.110.152.49";
  var IP10 = "128.110.152.74";
  var IP11 = "128.110.152.48";
  var IP12 = "128.110.152.61";
  var IP13 = "128.110.152.53";
  var IP14 = "128.110.152.84";
  var IP15 = "128.110.152.46";
  var IP16 = "128.110.152.62";


  //To display tabs
  var nodeResp=document.getElementById('NodeResp').value;
  $(function() {
    $( "#tabs" ).tabs({
      active: nodeResp-1
    });

  });

  //Initializing Number of nodes for displaying family size.
  var nodes=document.getElementById('NodesCount').value;
  dps = initDataPoints(nodes);

  //Creating the Family Size Chart.
  var chart = new CanvasJS.Chart("chartContainer", {
    title:{
      text:"Family list size on nodes",
      fontSize:24,
      fontFamily:"Helvetica"
    },
    width:1760,
    axisX:{
      interval: 1
    },
    axisY:{
      title: "Family Size",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "column",
      indexLabel: "{y}",
      dataPoints: dps
    }]
  });
  chart.render();

  //Updating the Family Size Chart
  var updateChart = function (nodeVal, yVal, labelVal) {
    var dps = chart.options.data[0].dataPoints;
    dps[nodeVal] = {label: labelVal , y: yVal};
    chart.options.data[0].dataPoints = dps;
    chart.render();
  };

  //Displaying Error Plot  - 1
  var dps1 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart1 = new CanvasJS.Chart("chartContainer1", {
    title :{

    },
    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps1
    }]
  });

  var updateChart1 = function (xVal, yVal) {
    dps1.push({
      x: xVal,
      y: yVal
    });

    if (dps1.length > dataLength) {
      dps1.shift();
    }

    chart1.render();
  };

  //Creating Web Socket 1
  var ws1 = new WebSocket("ws://"+ IP1 +":8080/StreamData/discStream");

  ws1.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart1(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode1(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws1.onerror = function(event){
    console.log("Error on WebSocket 1", event)
  }

  //Displaying Error Plot Stream - 2
  var dps2 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart2 = new CanvasJS.Chart("chartContainer2", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps2
    }]
  });

  var updateChart2 = function (xVal, yVal) {
    dps2.push({
      x: xVal,
      y: yVal
    });

    if (dps2.length > dataLength) {
      dps2.shift();
    }

    chart2.render();
  };

  //Creating Web Socket 2
  var ws2 = new WebSocket("ws://"+ IP2 +":8080/StreamData/discStream");

  ws2.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart2(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode2(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws2.onerror = function(event){
    console.log("Error on WebSocket 2", event)
  }

  //Displaying Error Plot Stream - 3
  var dps3 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart3 = new CanvasJS.Chart("chartContainer3", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps3
    }]
  });

  var updateChart3 = function (xVal, yVal) {
    dps3.push({
      x: xVal,
      y: yVal
    });

    if (dps3.length > dataLength) {
      dps3.shift();
    }

    chart3.render();
  };

  //Creating Web Socket 3
  var ws3 = new WebSocket("ws://"+ IP3 +":8080/StreamData/discStream");

  ws3.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart3(Number(res[0]), Number(res[1]));
    }  else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode3(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws3.onerror = function(event){
    console.log("Error on WebSocket 3", event)
  }

  //Displaying Error Plot  - 4
  var dps4 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart4 = new CanvasJS.Chart("chartContainer4", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps4
    }]
  });

  var updateChart4 = function (xVal, yVal) {
    dps4.push({
      x: xVal,
      y: yVal
    });

    if (dps4.length > dataLength) {
      dps4.shift();
    }

    chart4.render();
  };

  //Creating Web Socket 4
  var ws4 = new WebSocket("ws://"+ IP4 +":8080/StreamData/discStream");

  ws4.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart4(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode4(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws4.onerror = function(event){
    console.log("Error on WebSocket 4", event)
  }

  //Displaying Error Plot  - 5
  var dps5 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart5 = new CanvasJS.Chart("chartContainer5", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps5
    }]
  });

  var updateChart5 = function (xVal, yVal) {
    dps5.push({
      x: xVal,
      y: yVal
    });

    if (dps5.length > dataLength) {
      dps5.shift();
    }

    chart5.render();
  };

  //Creating Web Socket 5
  var ws5 = new WebSocket("ws://"+ IP5 +":8080/StreamData/discStream");

  ws5.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart5(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode5(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws5.onerror = function(event){
    console.log("Error on WebSocket 5", event)
  }

  //Displaying Error Plot  - 6
  var dps6 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart6 = new CanvasJS.Chart("chartContainer6", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps6
    }]
  });

  var updateChart6 = function (xVal, yVal) {
    dps6.push({
      x: xVal,
      y: yVal
    });

    if (dps6.length > dataLength) {
      dps6.shift();
    }

    chart6.render();
  };

  //Creating Web Socket 6
  var ws6 = new WebSocket("ws://"+ IP6 +":8080/StreamData/discStream");

  ws6.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart6(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode6(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws6.onerror = function(event){
    console.log("Error on WebSocket 6", event)
  }

  //Displaying Error Plot  - 7
  var dps7 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart7 = new CanvasJS.Chart("chartContainer7", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps7
    }]
  });

  var updateChart7 = function (xVal, yVal) {
    dps7.push({
      x: xVal,
      y: yVal
    });

    if (dps7.length > dataLength) {
      dps7.shift();
    }

    chart7.render();
  };

  //Creating Web Socket 7
  var ws7 = new WebSocket("ws://"+ IP7 +":8080/StreamData/discStream");

  ws7.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart7(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode7(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws7.onerror = function(event){
    console.log("Error on WebSocket 7", event)
  }

  //Displaying Error Plot  - 8
  var dps8 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart8 = new CanvasJS.Chart("chartContainer8", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps8
    }]
  });

  var updateChart8 = function (xVal, yVal) {
    dps8.push({
      x: xVal,
      y: yVal
    });

    if (dps8.length > dataLength) {
      dps8.shift();
    }

    chart8.render();
  };

  //Creating Web Socket 8
  var ws8 = new WebSocket("ws://"+ IP8 +":8080/StreamData/discStream");

  ws8.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart8(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode8(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws8.onerror = function(event){
    console.log("Error on WebSocket 8", event)
  }

  //Displaying Error Plot  - 9
  var dps9 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart9 = new CanvasJS.Chart("chartContainer9", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps9
    }]
  });

  var updateChart9 = function (xVal, yVal) {
    dps9.push({
      x: xVal,
      y: yVal
    });

    if (dps9.length > dataLength) {
      dps9.shift();
    }

    chart9.render();
  };

  //Creating Web Socket 9
  var ws9 = new WebSocket("ws://"+ IP9 +":8080/StreamData/discStream");

  ws9.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart9(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode9(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws9.onerror = function(event){
    console.log("Error on WebSocket 9", event)
  }

  //Displaying Error Plot  - 10
  var dps10 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart10 = new CanvasJS.Chart("chartContainer10", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps10
    }]
  });

  var updateChart10 = function (xVal, yVal) {
    dps10.push({
      x: xVal,
      y: yVal
    });

    if (dps10.length > dataLength) {
      dps10.shift();
    }

    chart10.render();
  };

  //Creating Web Socket 10
  var ws10 = new WebSocket("ws://"+ IP10 +":8080/StreamData/discStream");

  ws10.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart10(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode10(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws10.onerror = function(event){
    console.log("Error on WebSocket 10", event)
  }

  //Displaying Error Plot  - 11
  var dps11 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart11 = new CanvasJS.Chart("chartContainer11", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps11
    }]
  });

  var updateChart11 = function (xVal, yVal) {
    dps11.push({
      x: xVal,
      y: yVal
    });

    if (dps11.length > dataLength) {
      dps11.shift();
    }

    chart11.render();
  };

  //Creating Web Socket 11
  var ws11 = new WebSocket("ws://"+ IP11 +":8080/StreamData/discStream");

  ws11.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart11(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode11(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws11.onerror = function(event){
    console.log("Error on WebSocket 11", event)
  }

  //Displaying Error Plot  - 12
  var dps12 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart12 = new CanvasJS.Chart("chartContainer12", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps12
    }]
  });

  var updateChart12 = function (xVal, yVal) {
    dps12.push({
      x: xVal,
      y: yVal
    });

    if (dps12.length > dataLength) {
      dps12.shift();
    }

    chart12.render();
  };

  //Creating Web Socket 12
  var ws12 = new WebSocket("ws://"+ IP12 +":8080/StreamData/discStream");

  ws12.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart12(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode12(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws12.onerror = function(event){
    console.log("Error on WebSocket 12", event)
  }

  //Displaying Error Plot  - 13
  var dps13 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart13 = new CanvasJS.Chart("chartContainer13", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps13
    }]
  });

  var updateChart13 = function (xVal, yVal) {
    dps13.push({
      x: xVal,
      y: yVal
    });

    if (dps13.length > dataLength) {
      dps13.shift();
    }

    chart13.render();
  };

  //Creating Web Socket 13
  var ws13 = new WebSocket("ws://"+ IP13 +":8080/StreamData/discStream");

  ws13.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart13(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode13(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws13.onerror = function(event){
    console.log("Error on WebSocket 13", event)
  }

  //Displaying Error Plot  - 14
  var dps14 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart14 = new CanvasJS.Chart("chartContainer14", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps14
    }]
  });

  var updateChart14 = function (xVal, yVal) {
    dps14.push({
      x: xVal,
      y: yVal
    });

    if (dps14.length > dataLength) {
      dps14.shift();
    }

    chart14.render();
  };

  //Creating Web Socket 14
  var ws14 = new WebSocket("ws://"+ IP14 +":8080/StreamData/discStream");

  ws14.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart14(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode14(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws14.onerror = function(event){
    console.log("Error on WebSocket 14", event)
  }

  //Displaying Error Plot  - 15
  var dps15 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart15 = new CanvasJS.Chart("chartContainer15", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps15
    }]
  });

  var updateChart15 = function (xVal, yVal) {
    dps15.push({
      x: xVal,
      y: yVal
    });

    if (dps15.length > dataLength) {
      dps15.shift();
    }

    chart15.render();
  };

  //Creating Web Socket 15
  var ws15 = new WebSocket("ws://"+ IP15 +":8080/StreamData/discStream");

  ws15.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart15(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode15(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws15.onerror = function(event){
    console.log("Error on WebSocket 15", event)
  }

  //Displaying Error Plot  - 16
  var dps16 = []; // dataPoints

  var updateInterval = 100;
  var dataLength = 20000000; // number of dataPoints visible at any point

  var chart16 = new CanvasJS.Chart("chartContainer16", {
    title :{

    },

    axisX: {
      includeZero: true,
      title: 'Time (Seconds)',
    },
    axisY: {
      includeZero: true,
      title: "Relative Error",
      suffix: "%",
      gridColor: "#b0b5bc"
    },
    data: [{
      type: "line",
      dataPoints: dps16
    }]
  });

  var updateChart16 = function (xVal, yVal) {
    dps16.push({
      x: xVal,
      y: yVal
    });

    if (dps16.length > dataLength) {
      dps16.shift();
    }

    chart16.render();
  };

  //Creating Web Socket 16
  var ws16 = new WebSocket("ws://"+ IP16 +":8080/StreamData/discStream");

  ws16.onmessage = function(event) {
    var str = event.data;
    if(str.includes("EstC")==1) {
      var line = str.split("::");
      var res = line[1].split(",");
      updateChart16(Number(res[0]), Number(res[1]));
    } else if(str.includes("Node")==1) {
      var line = str.split("::");
      triggerNode16(line[1]);
    } else {
      var res = str.split("::");
      updateChart(Number(res[0])-1, Number(res[1]), "Node "+res[0]);
    }
  };

  ws16.onerror = function(event){
    console.log("Error on WebSocket 16", event)
  }

});

function initDataPoints(size) {
  var dps = [];
  for(var i=0; i<size; i++) {
    dps[i] = {label: "Node " + (i+1), y:0};
  }
  console.log(dps)
  return dps;
}

function execUpdate(event) {
  event.preventDefault();
  alert("New dataset has been uploaded.");
  $.ajax({
    type: "POST",
    url: "http://128.110.152.64:8080/StreamData/TriggerUpload",
  }).done(function() {
    alert("Complete uploading the new dataset.");
  });

}


function execClick(event) {
  event.preventDefault();
  alert("Please wait for the experiement to complete");
}

function execLiveClick(event) {
  event.preventDefault();
  alert("Live gossip is in progress.");
}