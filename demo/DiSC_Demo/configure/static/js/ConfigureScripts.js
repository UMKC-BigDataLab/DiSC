$(document).ready(function() {
  $("#submit").click(function() {
    $.blockUI({ message: '<div style="top:25%; position:relative;"><img src="../static/images/spinner.gif"/><br><br><br><p style="font-size: 20px;">Please Wait! The gossip procsses is being initialized.</p></div>' });
  });

  var $select1 = $('#select1'),
  $select2 = $('#select2'),
  $options = $select2.find('option');

  $select1.on('change', function() {
    $select2.html( $options.filter('[id="' + this.value + '"]'));
  }).trigger('change');
})

function execClick(event) {
  event.preventDefault();
  alert("Please start the experiement.");
}