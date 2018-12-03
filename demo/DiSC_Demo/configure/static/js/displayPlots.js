$(document).ready(function() {
  var ip = "128.110.154.247"

  //Display the score calculated.
  var req = ajax("http://" + ip + ":8080/StreamData/CalcScore",'GET');
  if (req.readyState == 4 && req.status == 200) {
     output = JSON.parse(req.responseText)
     $( "#score" ).append("<p class='tempFont2 font-weight-bold'>Estimated score: "+ output.EstScore + "</p><br><p class='tempFont2 font-weight-bold'>Actual score: "+ output.ActScore + "</p>" );
  }

  //Displaying the Average Time Convergance plots.
  var dataPoints1 = [];
  var dataPoints2 = [];
  var dataPoints3 = [];
  var dataPoints4 = [];
  var dataPoints5 = [];
  var dataPoints6 = [];
  var dataPoints7 = [];
  var dataPoints8 = [];
  var dataPoints9 = [];
  var dataPoints10 = [];
  var dataPoints11 = [];
  var dataPoints12 = [];
  var dataPoints13 = [];
  var dataPoints14 = [];
  var dataPoints15 = [];
  var dataPoints16 = [];

  var req = ajaxReq('http://' + ip + ':8080/StreamData/AvgSummary','GET');
  req.onreadystatechange = function() {
    if (req.readyState == 4 && req.status == 200) {
      output = JSON.parse(req.responseText);
      Object.keys(output).forEach(function(key) {
        if(key == "\"Node 1\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints1.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 2\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints2.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 3\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints3.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 4\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints4.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 5\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints5.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 6\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints6.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 7\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints7.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 8\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints8.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 9\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints9.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 10\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints10.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 11\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints11.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 12\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints12.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 13\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints13.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 14\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints14.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 15\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints15.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        } else if(key == "\"Node 16\"") {
          val = output[key];
          Object.keys(val).forEach(function(inKey) {
            dataPoints16.push({
              label: Number(inKey),
              y: Number(val[inKey])
            });
          });
        }

      });
      chart1.render();
      chart2.render();
      chart3.render();
      chart4.render();
    }
  };

  var chart1 = new CanvasJS.Chart("chartContainer1", {
    theme:"light2",
    animationEnabled: true,
    title:{
      text: "DiSC Convergence Speed"
    },
    axisY :{
      includeZero: true,
      title: "Avg. Relative Error",
      suffix: "%"
    },
    axisX :{
      includeZero: true,
      title: "Time (Seconds)",
    },
    toolTip: {
      shared: "true"
    },
    legend:{
      cursor:"pointer",
      itemclick : toggleDataSeries
    },
    data: [{
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 1",
      dataPoints: dataPoints1
    },
    {
      type: "line",
      showInLegend: true,
      visible: true,
      yValueFormatString: "##.###%",
      name: "Node 2",
      dataPoints: dataPoints2
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 3",
      dataPoints: dataPoints3
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 4",
      dataPoints: dataPoints4
    }]
  });

  var chart2 = new CanvasJS.Chart("chartContainer2", {
    theme:"light2",
    animationEnabled: true,
    title:{
      text: "DiSC Convergence Speed"
    },
    axisY :{
      includeZero: true,
      title: "Avg. Relative Error",
      suffix: "%"
    },
    axisX :{
      includeZero: true,
      title: "Time (Seconds)",
    },
    toolTip: {
      shared: "true"
    },
    legend:{
      cursor:"pointer",
      itemclick : toggleDataSeries
    },
    data: [{
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 5",
      dataPoints: dataPoints5
    },
    {
      type: "line",
      showInLegend: true,
      visible: true,
      yValueFormatString: "##.###%",
      name: "Node 6",
      dataPoints: dataPoints6
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 7",
      dataPoints: dataPoints7
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 8",
      dataPoints: dataPoints8
    }]
  });

  var chart3 = new CanvasJS.Chart("chartContainer3", {
    theme:"light2",
    animationEnabled: true,
    title:{
      text: "DiSC Convergence Speed"
    },
    axisY :{
      includeZero: true,
      title: "Avg. Relative Error",
      suffix: "%"
    },
    axisX :{
      includeZero: true,
      title: "Time (Seconds)",
    },
    toolTip: {
      shared: "true"
    },
    legend:{
      cursor:"pointer",
      itemclick : toggleDataSeries
    },
    data: [{
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 9",
      dataPoints: dataPoints9
    },
    {
      type: "line",
      showInLegend: true,
      visible: true,
      yValueFormatString: "##.###%",
      name: "Node 10",
      dataPoints: dataPoints10
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 11",
      dataPoints: dataPoints11
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 12",
      dataPoints: dataPoints12
    }]
  });

  var chart4 = new CanvasJS.Chart("chartContainer4", {
    theme:"light2",
    animationEnabled: true,
    title:{
      text: "DiSC Convergence Speed"
    },
    axisY :{
      includeZero: true,
      title: "Avg. Relative Error",
      suffix: "%"
    },
    axisX :{
      includeZero: true,
      title: "Time (Seconds)",
    },
    toolTip: {
      shared: "true"
    },
    legend:{
      cursor:"pointer",
      itemclick : toggleDataSeries
    },
    data: [{
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 13",
      dataPoints: dataPoints13
    },
    {
      type: "line",
      showInLegend: true,
      visible: true,
      yValueFormatString: "##.###%",
      name: "Node 14",
      dataPoints: dataPoints14
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 15",
      dataPoints: dataPoints15
    },
    {
      type: "line",
      visible: true,
      showInLegend: true,
      yValueFormatString: "##.###%",
      name: "Node 16",
      dataPoints: dataPoints16
    }]
  });

  chart1.render();
  chart2.render();
  chart3.render();
  chart4.render();

});

function toggleDataSeries(e) {
  if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible ){
    e.dataSeries.visible = false;
  } else {
    e.dataSeries.visible = true;
  }
  chart1.render();
  chart2.render();
  chart3.render();
  chart4.render();
}

var ajaxReq = function(url, type) {
  output = "";
  var req = new XMLHttpRequest();
  req.open(type, url, true);
  req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
  req.send("");
  return req;
};
