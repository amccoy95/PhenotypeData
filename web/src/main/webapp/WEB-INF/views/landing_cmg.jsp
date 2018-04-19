<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>

<t:genericpage>

    <jsp:attribute name="title">${pageTitle} landing page | IMPC Phenotype Information</jsp:attribute>

	<jsp:attribute name="breadcrumb">&nbsp;&raquo; <a href="${baseUrl}/biological-system">Biological Systems</a> &nbsp;&raquo; ${pageTitle}</jsp:attribute>

    <jsp:attribute name="header">

	<!-- CSS Local Imports -->
    <link href="${baseUrl}/css/alleleref.css" rel="stylesheet" />  
    <link href="${baseUrl}/css/biological_system/style.css" rel="stylesheet" />  
	
	<script type='text/javascript' src='${baseUrl}/js/charts/highcharts.js?v=${version}'></script>
    <script type='text/javascript' src='${baseUrl}/js/charts/highcharts-more.js?v=${version}'></script>
    <script type='text/javascript' src='${baseUrl}/js/charts/modules/exporting.js?v=${version}'></script>
	
  	<style>
		/* Override allele ref style for datatable */
		table.dataTable thead tr {
			display: table-row;
		}
		
		#cmg-genes_length {
		   	width: 50%;
		   	float: left;
		   	/* text-align: right; */
		}
		
		#cmg-genes_filter {
			width: 50%;
		   	float: right;
		   	text-align: right;
		}
	</style>

	</jsp:attribute>

    <jsp:attribute name="bodyTag"><body  class="phenotype-node no-sidebars small-header"></jsp:attribute>

    <jsp:attribute name="addToFooter">
		<div class="region region-pinned">

             <div id="flyingnavi" class="block smoothScroll">

                 <a href="#top"><i class="fa fa-chevron-up"
					title="scroll to top"></i></a>

                 <ul>
                     <li><a href="#top">Center for Mendelian Genomics</a></li>
                     <li><a href="#status">Gene status</a></li>
                     <li><a href="#table">Gene table</a></li>
                     <li><a href="#vignettes">Vignettes</a></li>
                     <li><a href="#phenotypes-distribution">Phenotype Distribution</a></li>
                 </ul>

                 <div class="clear"></div>

             </div>

         </div>
		
	</jsp:attribute>
    <jsp:body>

        <div class="region region-content">
            <div class="block block-system">
                <div class="content">
                    <div class="node node-gene">
                        <h1 class="title" id="top">${pageTitle}</h1>
						
						<div style="padding: 30px;" class="clear both"></div>
						
                        <div class="section" id="">
                            <div class="inner">
	                            	<h1>
	                            		<img src="${baseUrl}/img/landing/cmg-logo_1.png" alt="Centers for Mendelian Genetics logo" style="max-width: 35%; float: left;">
	                            		<p style="color: #23bcb7; font-weight: bold; padding-top: 40px; float: right;">${pageTitle}</p>
	                            	</h1>
	                            	<br/><br/>
	                            	<div style="text-align: justify; clear: both;">
	                            		<p>The <a href='https://www.mendelian.org/' target='_blank'>Centers for Mendelian Genomics</a> (CMG) is an NIH funded project to use genome-wide sequencing and other genomic approaches to discover the genetic basis underlying as many human Mendelian traits as possible.  The IMPC is helping CMG validate human disease gene variants by creating and characterizing orthologous knockout mice.</p>
	                            	</div>
                            		<br/><br/>
                            </div>
                        </div>
					   
					   <div class="section" id="status">
					   		<h2 class="title">Status of CMG genes in mouse lines in IMPC</h2>
					   		<div class="inner" style="height: 450px;">
				                <!-- Column Chart Tier1 -->
					   			<div class="half">
					   				<div id="columnChart1"> <script type="text/javascript"> ${ columnChart1 } </script> </div>
				                </div>
				                
				                <!-- Column Chart Tier2 -->
				                <div class="half">
				                    <div id="columnChart2"><script type="text/javascript"> ${ columnChart2 } </script></div> 
				                </div>
				                <br/><br/>
					   		</div>
					   	</div>
						
					   	<div class="section" id="table">
					   		<h2 class="title">Table of CMG genes in mouse lines in IMPC</h2>
					   		<div class="inner">
					   			<!-- TABLE -->
					   			<table id="cmg-genes" class="table tableSorter">
				   					<thead>
				   						<tr>
				   							<th rowspan="2" colspan="1" class="headerSort">Disease</th>
				   							<th rowspan="2" colspan="1" class="headerSort">OMIM ID</th>
				   							<th rowspan="2" colspan="1" class="headerSort">Tier1 gene</th>
				   							<th rowspan="2" colspan="1" class="headerSort">Tier2 gene</th>
				   							<th rowspan="2" colspan="1" class="headerSort">Mouse Orthologue(s)</th>
				   							<th rowspan="2" colspan="1" class="headerSort">IMPC Status</th>
				   							<th rowspan="1" colspan="3" class="headerSort" style="text-align: center;">Phenotype Overlap Score</th>
				   						</tr>
				   						<tr>
				   							<%-- <th rowspan="1" colspan="1" class="headerSort">Other_Human_Disease</th> --%>
				   							<th rowspan="1" colspan="1" class="headerSort">IMPC mouse</th>
				   							<th rowspan="1" colspan="1" class="headerSort">Published Mouse</th>
				   						</tr>
				   					</thead>
				   					<!-- Body -->
				   				</table>
								<div id="tsv-result" style="display: none;"></div>
								<br/>
								<div id="export">
				                  	<p class="textright">
				                      	Download data as:
				                      	<a id="downloadTsv" class="button fa fa-download">TSV</a>
				   						<a id="downloadExcel" class="button fa fa-download">XLS</a>
				                  	</p>
				              	</div>
				   			</div>
					   	</div>
					   	
					   	<script type='text/javascript'>
							$(document).ready(function() {
								/* console.log(${cmg_genes}); */
								$('#cmg-genes').DataTable({
									"bDestroy" : true,
									"searching" : true,
									"bPaginate" : true,
									"sPaginationType" : "bootstrap",
									// "scrollX": true,
									"initComplete": function(settings, json) {
						                $('.dataTables_scrollBody thead tr').css({visibility:'collapse'});
						            },
									/* "columnDefs": [
										{ "type": "alt-string", targets: 3 }   //4th col sorted using alt-string
									],  */
									"aaSorting": [[0, "asc"]], // 0-based index
									"aoColumns": [
									    null, null,null,
//										 {"sType": "html", "bSortable": true},
//										 {"sType": "string", "bSortable": true},
//										 {"sType": "string", "bSortable": true},
										 {"sType": "html", "bSortable": true}
									],
									"aaData": ${cmg_genes},
							        "aoColumns": [
							            { "mDataProp": "disease"},
							            { "mDataProp": "omim_id",
								            	"render": function ( data, type, full, meta ) {
					                		 		return '<a href="http://www.mousephenotype.org/data/disease/'+data+'" target="_blank">'+data+'</a>';
					                		 	}
							            },
							            { "mDataProp": "tier_1_gene"},
							            { "mDataProp": "tier_2_gene"},
							            { "mDataProp": "mouse_orthologue",
					                        "render": function ( data, type, full, meta ) {
					                        		if (data == "") {
					                        			return 'NA';
					                        		}
					                		 		return '<a href="'+full.link_IMPC+'" target="_blank" title="' + full.support_count + ' inference methods support this orthologue: ' + full.support + '">'+ data +'</a>';
					                		 	}
					                 	},
							            { "mDataProp": "impc_status",
					                 	  "sWidth": "15%"
					                 	},
							            /* { "mDataProp": "other_human_disease"}, */
							            { "mDataProp": "impc_mouse",
								            	"render": function ( data, type, full, meta ) {
					                        		var num = parseFloat(data);
					                         	if (num > 0 && num != NaN ) {
					                         		return num.toFixed(2);
					                         	}
				                		 			return data;
					                        }
							            },
							            { "mDataProp": "published_mouse",
					                        "render": function ( data, type, full, meta ) {
					                        		var num = parseFloat(data);
					                         	if (num > 0 && num != NaN ) {
					                         		return num.toFixed(2);
					                         	}
				                		 			return data;
					                        }
				                		 	},
							       	]
								});
								
								
								
								var tsv = jsonToTsv( ${cmg_genes} );
								$('#tsv-result').html( tsv );
							   
								function jsonToTsv (input){
							        var json = input,
							            tsv = '',
							            firstLine = [],
							            lines = [];


							        // Helper to add double quotes if
							        // the value is string
							        var addQuotes = function(value){
							            if (isNaN(value)){
							                /* return '"'+value+'"'; */
							                return value;
							            }
							            return value;
							        };
							        $.each(json, function(index, item){
							            var newLine = [];
							            $.each(item, function(key, value){
							                if (index === 0){
							                    firstLine.push(addQuotes(key));
							                }
							                newLine.push(addQuotes(value));
							            });
							            lines.push(newLine.join('\t'));
							        });
							        tsv = firstLine.join('\t');
							        tsv += '\n'+lines.join('\n');
							        return tsv;
							    }; 
							    
							    function downloadInnerHtml(filename, elId, mimeType) {
							        var elHtml = document.getElementById(elId).innerHTML;
							        var link = document.createElement('a');
							        mimeType = mimeType || 'text/plain';

							        link.setAttribute('download', filename);
							        link.setAttribute('href', 'data:' + mimeType + ',' + encodeURIComponent(elHtml));
							        document.body.appendChild(link);
							        link.click(); 
							        document.body.removeChild(link);
							    }

							    var fileNameTsv = 'cmg-genes.tsv'; // You can use the .txt extension if you want
							    var fileNameExcel = 'cmg-genes.xls'; // You can use the .txt extension if you want

							    $('#downloadTsv').click(function(){
							        downloadInnerHtml(fileNameTsv, 'tsv-result','text/html');
							    });
							    
							    $('#downloadExcel').click(function(){
							        downloadInnerHtml(fileNameExcel, 'tsv-result','application/xls;charset=utf-8');
							    });
							    
							    
							    
							    
							   	
                                
							});
						</script>
					   	
					   	<br/><br/>
                        	<div class="section" id="vignettes">
                            <h2>Vignettes</h2>
                            <div class="inner">
                            		
                            </div>
                        </div>
	                            
                        <div class="section" id="phenotypes-distribution">
                            <h2 class="title">Phenotypes distribution</h2>
                            <div class="inner">
                            		<p></p>
                                	<br/> <br/>
                               	<!--  <div id="phenotypeChart">
                                    	<script type="text/javascript"> $(function () {  ${phenotypeChart} }); </script>							
                                	</div> -->
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>

    </jsp:body>

</t:genericpage>



