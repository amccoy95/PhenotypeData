<%--
  Created by IntelliJ IDEA.
  User: ckc
  Date: 23/02/2016
  Time: 10:37
  To change this template use File | Settings | File Templates.
--%>


<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<!-- always show phenotype icons -->
<%--<jsp:include page="phenotype_icons_frag.jsp"/>--%>
<c:choose>

  <c:when test="${summaryNumber > 0}">

    <jsp:include page="phenotype_icons_frag.jsp"/>

    <%--<c:if test="${!(empty dataMapList)}">--%>
      <%--<br/>--%>
      <%--<!-- best example http://localhost:8080/PhenotypeArchive/genes/MGI:1913955 -->--%>

      <%--<div class="floatleft marginup"--%>
           <%--style="clear: both">--%>
        <%--<p>--%>
          <%--<a class="btn"--%>
             <%--href='${baseUrl}/experiments?geneAccession=${gene.mgiAccessionId}'>All Adult Phenotype</a>--%>
        <%--</p>--%>
      <%--</div>--%>
    <%--</c:if>--%>

    <%--<c:if test="${gene.embryoDataAvailable}">--%>
      <%--<div class="floatleft marginup"--%>
           <%--style="clear: both">--%>
        <%--<a class="btn"--%>
           <%--href="${drupalBaseUrl}/embryoviewer?mgi=${acc}">Embryo Viewer</a>--%>
      <%--</div>--%>
    <%--</c:if>--%>

    <div id="phenoSumDiv">
        <h5 class="sectHint">All Phenotypes Summary</h5>
        <p>Based on automated MP annotations supported by experiments
      on knockout mouse models. Click on icons to go to all ${gene.markerSymbol} data for that phenotype.</p>
        <p></p>
        <c:if test="${!(empty dataMapList)}">
            <!-- best example http://localhost:8080/PhenotypeArchive/genes/MGI:1913955 -->
            <a id="allAdultDataBtn" class="btn" href='${baseUrl}/experiments?geneAccession=${gene.mgiAccessionId}'>All ${gene.markerSymbol} Measurements</a>
        </c:if>
        <c:if test="${gene.embryoDataAvailable}">
            <a id="embryoViewerBtn" class="btn" href="${drupalBaseUrl}/embryoviewer?mgi=${acc}">3D Embryo Imaging</a>
        </c:if>

        <c:if test="${hasVignette}">
            <a class="btn" href="${baseUrl}/embryo/vignettes#${acc}">Embryo Vignette</a>
        </c:if>
       

        <jsp:include page="heatmapFrag.jsp"/>
							
				
       

       
    </div>
  </c:when>



  <c:when test="${summaryNumber == 0}">

			
							
							
							
							
    <c:if test="${empty dataMapList && empty phenotypes}"><!-- no postQC data -->
    
	    <c:choose>
	    	<c:when test="${ attemptRegistered && phenotypeStarted }">
	    	<h5>Phenotyping has started, data coming soon.</h5>
	
	         <p><!-- No hits that meet the p value threshold. --><jsp:include page="heatmapFrag.jsp"/></p>
	    	</c:when>
	    	<c:when  test="${attemptRegistered}">
		        <div class="alert alert-info">
		          <h5>Registered for phenotyping</h5>
		
		          <p>Phenotyping is planned for a knockout strain of this gene but
		            data is not currently available.</p>
		        </div>
	    
	    	</c:when>
	    	<c:when test="${!attemptRegistered}">
		        <div class="alert alert-info">
		          <h5>Not currently registered for phenotyping</h5>
		
		          <p>Phenotyping is currently not planned for a knockout strain of this gene.
		          </p>
		        </div>
		      
		      	<br/>
	    	</c:when>
	    </c:choose>
    </c:if>
     

     
    <c:if test="${!(empty dataMapList) && empty phenotypes}">
      <div class="alert alert-info">
        <h5>No Significant Phenotype Associations Found</h5>

        <p>No significant phenotype associations were found with data that has
          passed quality control (QC), but you can click
          on the "All Adult Data" button to see all phenotype data that has
          passed QC. Preliminary phenotype assocations
          may appear with new pre-QC phenotype data.</p>
      </div>
      <br/>
      <!-- best example http://localhost:8080/PhenotypeArchive/genes/MGI:1913955 -->
      <div class="floatright marginup" style="clear: both">
        <a id="allAdultDataBtn" class="btn" href='${baseUrl}/experiments?geneAccession=${gene.mgiAccessionId}'>All Adult Data</a>
      </div>
        <div class="clear"></div>
    </c:if>

    <c:if
            test="${gene.embryoDataAvailable}">
      <div class="floatright marginup"
           style="clear: both">
        <a class="btn"
           href="${drupalBaseUrl}/embryoviewer?mgi=${acc}">3D Imaging</a>
      </div>
    </c:if>
  </c:when>
  <c:when test="${hasPreQcData}">
    <!-- Only pre QC data available, suppress post QC phenotype summary -->
  </c:when>
  <c:otherwise>
    <div class="alert alert-info">There are currently no IMPC phenotype associations
      for the gene ${gene.markerSymbol} </div>
    <br/>
  </c:otherwise>
</c:choose>

<div id="phenotypes"></div> <!-- Empty anchor for links, used for disease paper. Don't remove.  -->


<c:if
        test='${hasPreQcData || summaryNumber > 0 || phenotypes.size() > 0}'>
  <!-- Associations table -->
  <div id="phenotypeTableDiv" class="inner-division">
  <h5>Significant Phenotypes</h5>


  <div class="row-fluid">
    <div class="container span12">
      <br/>

      <div class="row-fluid" id="phenotypesDiv">

        <div class="container span12">
 
          <c:if test="${not empty phenotypes}">
            <form class="tablefiltering no-style" id="target" action="destination.html">
           
              <c:forEach
                      var="phenoFacet" items="${phenoFacets}"
                      varStatus="phenoFacetStatus">
                <select id="${phenoFacet.key}" class="impcdropdown"
                        multiple="multiple"
                        title="Filter on ${phenoFacet.key}">
                  <c:forEach
                          var="facet" items="${phenoFacet.value}">
                    <option>${facet.key}</option>
                  </c:forEach>
                </select>
              </c:forEach>
           
              <div class="clear"></div>
            </form> 
            <div class="clear"></div>

            <c:set var="count" value="0" scope="page"/>
            <c:forEach
                    var="phenotype" items="${phenotypes}"
                    varStatus="status">
              <c:forEach
                      var="sex" items="${phenotype.sexes}">
                <c:set var="count" value="${count + 1}" scope="page"/>
              </c:forEach>
            </c:forEach>

            <jsp:include page="PhenoFrag.jsp"></jsp:include>
            <br/>

            <div id="exportIconsDiv"></div>
          </c:if>

          <!-- if no data to show -->
          <c:if
                  test="${empty phenotypes}">
            <div
                    class="alert alert-info">Pre QC data has been submitted
              for this gene. Once the QC process is finished phenotype
              associations stats will be made available.
            </div>
          </c:if>

        </div>
      </div>
    </div>
  </div>
  
  <div id="export">
	<p class="textright"> 
		Download data as: 
		<a id="tsvDownload" href="${baseUrl}/genes/export/${gene.getMgiAccessionId()}?fileType=tsv&fileName=${gene.markerSymbol}" target="_blank" class="button fa fa-download">TSV</a> 
		<a id="xlsDownload" href="${baseUrl}/genes/export/${gene.getMgiAccessionId()}?fileType=xls&fileName=${gene.markerSymbol}" target="_blank" class="button fa fa-download">XLS</a>
	</p>
 </div>
</div><!-- end of div for mini section line -->
</c:if>
