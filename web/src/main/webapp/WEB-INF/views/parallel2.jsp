<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:genericpage>
    <jsp:attribute name="title">Parrallel Coordinates Chart for ${procedure}</jsp:attribute>
    <jsp:attribute name="bodyTag"><body  class="gene-node no-sidebars small-header"></jsp:attribute>
    
    <jsp:attribute name="header">
                    
        <!-- CSS Local Imports -->
		<link rel="stylesheet" href="${baseUrl}/css/vendor/slick.grid.css" type="text/css" media="screen"/>
		<link rel="stylesheet" href="${baseUrl}/css/parallelCoordinates/style.css" type="text/css" />

        <!-- JavaScript Local Imports -->
		<script type='text/javascript' src="${baseUrl}/js/general/dropDownParallelCoordinatesPage.js?v=${version}"></script> 	
		<script type="text/javascript" src="${baseUrl}/js/vendor/d3/d3.v3.js"></script>
		<script type="text/javascript" src="${baseUrl}/js/vendor/d3/d3.js"></script>
		<script type="text/javascript" src="${baseUrl}/js/vendor/d3/d3.csv.js"></script>
		<script type="text/javascript" src="${baseUrl}/js/vendor/d3/d3.layout.js"></script>	
			 
		<script src="${baseUrl}/js/vendor/jquery/jquery.event.drag-2.0.min.js"></script>
		<script src="${baseUrl}/js/vendor/slick/slick.core.js"></script>
		<script src="${baseUrl}/js/vendor/slick/slick.grid.js"></script>
		<script src="${baseUrl}/js/vendor/slick/slick.dataview.js"></script>
		<script src="${baseUrl}/js/vendor/slick/slick.pager.js"></script>
		<script type="text/javascript">
			var base_url = '${baseUrl}';
		</script>		
		
    </jsp:attribute>
    
    <jsp:body>
    		
			<div class="content">
				<div class="section"> 
					<h2 class="title" id="data-summary">Parallel coordinates tool<span class="documentation" ><a href='' id='parallelPanel' class="fa fa-question-circle pull-right"></a></span> </h2>
					<div class="inner">
						<div id="spinner"><i class="fa fa-refresh fa-spin"></i></div>
						
						<form class="tablefiltering no-style" id="target" action="destination.html">
							<select id="proceduresFilter" class="impcdropdown"  multiple="multiple" title="Select procedures to display">
		                    	<c:forEach var="procedure" items="${procedures}">
		                    		<option value="${procedure.getStableId().substring(0,8)}">${procedure.getName()}</option>
		                    	</c:forEach>
		                    </select>
		                    <select id="centersFilter" class="impcdropdown"  multiple="multiple" title="Select centers to display">
		                    	<c:forEach var="center" items="${centers}">
		                    		<option value="${center}">${center}</option>
		                    	</c:forEach>
		                    </select>
		                    <div id="widgets_pc" class="widgets" class="right">
								
							</div>
		                	<div class="clear"></div>
	                    </form>
									
						<div id="chart-and-table">
						</div>
					</div>
				</div>
			</div>
	
	</jsp:body>
    
</t:genericpage>