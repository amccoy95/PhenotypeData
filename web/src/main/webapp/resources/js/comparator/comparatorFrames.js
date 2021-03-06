$(document).ready(function(){						
	
console.log('comparatorFrames.js ready');


var previousControlId=$(".clickable_image_control").first().attr('id');
console.log('control prev'+previousControlId)
var previousMutantId=$(".clickable_image_mutant").first().attr('id');

$(".clickable_image_control").click(function() {
		console.log( this.id );
		if(previousControlId){
			$('#'+previousControlId).toggleClass( "img_selected");
		}
		if(this.src.indexOf('_pdf')>-1){
			$('#control_frame').attr('src',googlePdf.replace('replace',pdfWithoutId+'/'+this.id));
		}
		else if($(this).attr('data-imageLink')){
			  console.log('data-imageLink url found='+$(this).attr('data-imageLink').replace('http:','https:'));
			  //$('#mutant_frame').attr('src',googlePdf.replace('replace',pdfWithoutId+'/'+this.id));//replace the placeholder string with the id string.
			  $('#control_frame').attr('src',$(this).attr('data-imageLink').replace('http:','https:'));//image data links from jax aren't relative https seems to work so default to this
		 }else{
			$('#control_frame').attr('src',jpegUrlDetailWithoutId+'/'+this.id);
		}
		
		$('#'+this.id).toggleClass( "img_selected");
		previousControlId=this.id;
		$('#control_annotation').text($(this).attr('oldtitle'));
});

$(".clickable_image_mutant").click(function() {
	  console.log('title='+ $(this).attr('oldtitle'));
	  if(previousMutantId){
		  $('#'+previousMutantId).toggleClass( "img_selected");
	  }
	  if(this.src.indexOf('_pdf')>-1){
		  $('#mutant_frame').attr('src',googlePdf.replace('replace',pdfWithoutId+'/'+this.id));//replace the placeholder string with the id string.
	  }else if($(this).attr('data-imageLink')){
		  console.log('data-imageLink url found='+$(this).attr('data-imageLink').replace('http:','https:'));
		  $('#mutant_frame').attr('src',$(this).attr('data-imageLink').replace('http:','https:'));//image data links from jax aren't relative https seems to work so default to this
	  }else
	 {
		  $('#mutant_frame').attr('src',jpegUrlDetailWithoutId+'/'+this.id);
	 }
	  $('#'+this.id).toggleClass( "img_selected");
	  previousMutantId=this.id;
	  //change the text under the main image to be the same as the title
	  $('#mutant_annotation').text($(this).attr('oldtitle'));
	  
	  
	});

$("#mutant_only_button").click(function() {
	  console.log('click on mutant only');
	  $('#control_box').toggle('no_box');//need to set this to display none instead of hidden which still tatkes up space
	  $('#mutant_box').toggleClass('half_box_right full_box');
	  $('#mutant_frame').toggleClass('full_frame');
	  if($('#mutant_only_button').text() === 'Display Mutant Only'){
		  $('#mutant_only_button').text('Display WT and Mutant');
	  }else{
		  $('#mutant_only_button').text('Display Mutant Only');
	  }
	  console.log('mutant: '+$('#mutant_only_button').attr('value'));
	  //$('#mutant_only_button').toggleAttr('value','full_frame');
	});

});