<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>

<t:genericpage>

    <jsp:attribute name="title">${pageTitle} landing page | IMPC Phenotype Information</jsp:attribute>

    <jsp:attribute name="breadcrumb">&nbsp;&raquo; <a href="${baseUrl}/landing">Landing
        Pages</a> &nbsp;&raquo; ${pageTitle}</jsp:attribute>

    <jsp:attribute name="header">

	<!-- CSS Local Imports -->
    <link href="${baseUrl}/css/alleleref.css" rel="stylesheet" />
    <link href="${baseUrl}/css/heatmap.css" rel="stylesheet" />

	<script type='text/javascript' src='${baseUrl}/js/charts/highcharts.js?v=${version}'></script>
    <script type='text/javascript' src='${baseUrl}/js/charts/highcharts-more.js?v=${version}'></script>
    <%-- <script type='text/javascript' src='${baseUrl}/js/charts/modules/exporting.js?v=${version}'></script> --%>
    <%-- <script type='text/javascript' src='${baseUrl}/js/charts/modules/export-data.js?v=${version}'></script> --%>
    <%-- <script type='text/javascript' src='${baseUrl}/js/charts/modules/export-csv.js?v=${version}'></script> --%>
    
    <script type='text/javascript' src='${baseUrl}/js/charts/modules/heatmap.js?v=${version}'></script> 
    <script type="text/javascript" src='${baseUrl}/js/charts/heatMapChart.js?v=${version}'></script>
    <script src="http://blacklabel.github.io/grouped_categories/grouped-categories.js"></script>
    

	</jsp:attribute>


    <jsp:attribute name="bodyTag"><body  class="phenotype-node no-sidebars small-header"></jsp:attribute>

    <jsp:attribute name="addToFooter">

	</jsp:attribute>
    <jsp:body>

        <div class="region region-content">
            <div class="block block-system">
                <div class="content">
                    <div class="node node-gene">
                        <h1 class="title" id="top">${pageTitle} </h1>

                        <c:import url="landing_overview_frag.jsp"/>
						
						<div style="padding: 30px;" class="clear both"></div>
						
                        <div class="section">
                            <h2 class="title">Approach</h2>
                            <div class="inner">
                            		<p>To identify the function of genes, the IMPC uses a series of standardised protocols described in <a href="${baseUrl}/../impress">IMPReSS</a> (International Mouse Phenotyping Resource of Standardised Screens). 
                            		Tests addressing the metabolic function are conducted on young adults at 11-16 weeks of age. Developmental issues or problems are evaluated in the embryonic pipeline.</p>
                            		<br/><br/>
                            		
                                	<c:import url="landing_procedures_frag.jsp"/>
                            </div>
                        </div>
					   
					   	<div class="section">
					   		<h2 class="title">IMPC Metabolism Publication</h2>
					   		<div class="inner">
					   			<h3>Metabolic diseases investigated in 2,016 knockout mouse lines</h3>
					   			<p><a href="">Nature Communications</a></p>
					   			<ul>
					   				<li>974 genes associated to strong metabolic phenotypes.</li>
					   				<li>429 genes had not been previously associated with metabolism, 51 completely lacked functional annotation, and 25 have single nucleotide polymorphisms associated to human metabolic disease phenotypes.</li>
					   				<li>515 genes linked to at least one disease in OMIM.</li>
					   				<li>Networks of co-regulated genes were identified, and genes of predicted metabolic function identified.</li>
					   				<li>Pathway mapping revealed sexual dimorphism in genes and pathways.</li>
					   				<li>This investigation is based on about 10% of mammalian protein-coding genes. The IMPC will continue screening for genes associated to metabolic diseases in its second 5 year phase.</li>
					   			</ul>
					   			
					   			<p>Press releases: 
					   				<a href="">EMBL-EBI</a>&nbsp;|&nbsp;
									<a href="">MRC</a>&nbsp;|&nbsp;
									<a href="">IMPC</a>
									<!--&nbsp;|&nbsp;<a href="">Sanger</a> -->
								</p>
								
								<h3>Methods</h3>
                               	<p><b>A</b>nalyses are based on <b>seven metabolic parameters</b> with diagnostic relevance in human clinical research:</p>
                               	<ul>
                               		<li>Fasting basal blood glucose level before glucose tolerance test (T0)</li>
                               		<li>Area under the curve of blood glucose level after intraperitoneal glucose administration relative to basal blood glucose level (AUC)</li>
                               		<li>Plasma triglyceride levels (TG)</li>
                               		<li>Body mass (BM)</li>
                               		<li>Metabolic rate (MR)</li>
                               		<li>Oxygen consumption rate (VO2)</li>
                               		<li>Respiratory exchange ratio (RER) – a measure of whole-body metabolic fuel utilization</li>                               		
                               	</ul>
                               	<p>Mutant/wildtype ratios (mean value of metabolic parameters of mutants divided by the mean value obtained for wildtypes) were calculated:</p>
                               	<ul>
                               		<li>Control wildtype mice from each phenotypic center included, matched for gender, age, phenotypic pipeline and metadata (e.g. instrument).</li>
                               		<li>Males and females analyzed separately to account for sexual dimorphism.</li>
                               	</ul>
                               	
                               	<h3>New mouse models</h3>
                               	<ul>
                               		<li>IMPC generated and identified new genetic disease models.</li>
                               		<li>New models available to the research community to perform in-depth investigation of novel genetic elements associated to metabolic disease mechanisms.</li>
                               		<li>These models fill the gap between genome-wide association studies and functional validation using a mammalian model organism.</li>
                               	</ul>
                               	
                               	<h3>Gene table</h3>
                               	<p>Mutant/wildtype ratios below the 5th percentile and above the 95th percentile of the ratio distributions yielded 28 gene lists that serve as a data mining resource for further investigation into potential links to human metabolic disorders.</p>
                                <br/> <br/>
                               	<div id="heatMapContainer" style="height: 450px; min-width: 310px; max-width: 1000px;"></div>
                                <script>
                                		drawHeatMap();
                                </script>
	                            
					   		</div>
					   		
					   	</div>

                        <div class="section">
                            <h2>
                                Vignettes
                            </h2>
                            <div class="inner"></div>
                        </div>
	                            
                        <div class="section">
                            <h2 class="title">Phenotypes distribution</h2>
                            <div class="inner">
                            		<p></p>
                                <br/> <br/>
                                <div id="phenotypeChart">
                                    <script type="text/javascript"> $(function () {  ${phenotypeChart} }); </script>							
                                </div>
                                 	
                            </div>
                        </div>
                        
                        <div class="section">
                            <jsp:include page="paper_frag.jsp"></jsp:include>
                        </div>


                    </div>
                </div>
            </div>
        </div>

    </jsp:body>

</t:genericpage>


