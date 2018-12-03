$(document).ready(function() {
  $("#submit").click(function() {1
    $.blockUI({ message: '<div style="top:25%; position:relative;"><img src="../static/images/spinner.gif"/><br><br><br><p style="font-size: 20px;">Please Wait! The gossip procsses is being initialized.</p></div>' });
  })
})
