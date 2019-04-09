<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>


<c:set var="count" value="0" scope="page"/>
<c:set var="maleCount" value="0" scope="page"/>
<c:set var="femaleCount" value="0" scope="page"/>
<c:set var="noSexCount" value="0" scope="page"/>
<c:forEach var="phenotype" items="${rowsForPhenotypeTable}" varStatus="status">
    <c:forEach var="sex" items="${phenotype.sexes}">
        <c:set var="count" value="${count + 1}" scope="page"/>
        <c:if test='${sex.equalsIgnoreCase("male")}'>
            <c:set var="maleCount" value="${maleCount + 1}" scope="page"/>
        </c:if>
        <c:if test='${sex.equalsIgnoreCase("female")}'>
            <c:set var="femaleCount" value="${femaleCount + 1}" scope="page"/>
        </c:if>
        <c:if test='${sex.equalsIgnoreCase("no_data")}'>
            <c:set var="noSexCount" value="${noSexCount + 1}" scope="page"/>
        </c:if>
    </c:forEach>
</c:forEach>


<!-- <script>
$(document).ready(function() {
$('#genes').DataTable({responsive: true});
} );
var resTemp = document.getElementsByClassName("resultCount");
if (resTemp.length > 1)
resTemp[0].remove();
</script> -->


<table id="genes" class="table dt-responsive clickableRows" style="width:100%;">
    <thead>
    <tr>
        <th class="headerSort">Phenotype</th>
        <th class="headerSort">System</th>
        <th class="headerSort">Allele</th>
        <th class="headerSort" title="Zygosity">Zyg</th>
        <th class="headerSort">Sex</th>
        <th class="headerSort">Life Stage</th>


        <!-- <th class="headerSort">Source</th> -->
        <th>P Value</th>
        <!--th class="headerSort">Data</th-->
    </tr>
    </thead>
    <tbody>
    <c:forEach var="phenotype" items="${rowsForPhenotypeTable}" varStatus="status">
        <c:set var="europhenome_gender" value="Both-Split"/>
        <tr>

            <td>

                <c:if test="${ empty phenotype.phenotypeTerm.id }">
                    ${phenotype.phenotypeTerm.name}
                </c:if>
                <c:if test="${not empty phenotype.phenotypeTerm.id}">
                    <%--a href="${baseUrl}/phenotypes/${phenotype.phenotypeTerm.id}">${phenotype.phenotypeTerm.name}</a--%>
                    <span>${phenotype.phenotypeTerm.name}</span>
                </c:if>

            </td>
            <td class="text-lg-center" style="font-size: 1.25em;">
                <span class="row_abnormalities">
                    <c:set var="marginLeftCount" value="0"/>
                    <c:forEach var="topLevelMpGroup" items="${phenotype.topLevelMpGroups }" varStatus="groupCount">
                        <c:choose>
                            <c:when test="${topLevelMpGroup eq 'NA' }">
                                <%-- <div title="${topLevelMpGroup}" >${topLevelMpGroup}</div> don't display a top level icon if there is no top level group for the top level mp term--%>
                            </c:when>
                            <c:otherwise>

                                <i class="${phenotypeGroupIcons[phenotypeGroups.indexOf(topLevelMpGroup)]} text-primary"
                                   data-hasqtip="27" title="${topLevelMpGroup}"></i>
                            </c:otherwise>
                        </c:choose>

                    </c:forEach>
                </span>
            </td>
            <td>
                <!-- note that allele page takes mgi GENE id not allele id -->
                <%--a
                        href="${baseUrl}/alleles/${acc}/${phenotype.allele.superScript}"><t:formatAllele>${phenotype.allele.symbol}</t:formatAllele>
                </a--%>
                <span><t:formatAllele>${phenotype.allele.symbol}</t:formatAllele></span>

            </td>
            <td title="${phenotype.zygosity}">${phenotype.zygosity.getShortName()}</td>
            <td>
                <c:set var="count" value="0" scope="page"/>
                <t:displaySexes sexes="${phenotype.sexes}"></t:displaySexes>
            </td>
            <td>${phenotype.lifeStageName} <%-- length= ${phenotype.phenotypeCallUniquePropertyBeans} --%></td>


            <td data-sort="${phenotype.prValueAsString}">
                <t:formatScientific>${phenotype.prValueAsString}</t:formatScientific></td>


            <c:if test="${phenotype.getEvidenceLink().getDisplay()}">
                <c:if test='${phenotype.getEvidenceLink().getIconType().name().equalsIgnoreCase("IMAGE")}'>
                    <td data-sort="${phenotype.getEvidenceLink().getUrl() }">

                    </td>
                </c:if>
                <c:if test='${phenotype.getEvidenceLink().getIconType().name().equalsIgnoreCase("GRAPH")}'>
                    <td data-sort="${phenotype.getEvidenceLink().getUrl() }&pageTitle=${phenotype.phenotypeTerm.name} phenotype with knockout ${gene.markerSymbol}&linkBack=linkback link here">

                    </td>
                </c:if>
                <c:if test='${phenotype.getEvidenceLink().getIconType().name().equalsIgnoreCase("TABLE")}'>
                    <td data-sort="${phenotype.getEvidenceLink().getUrl() }">

                    </td>
                </c:if>
            </c:if>

            <c:if test="${phenotype.getImagesEvidenceLink().getDisplay()}">
                <td data-sort="${phenotype.getImagesEvidenceLink().url}">

                </td>
            </c:if>

            <c:if test="${!phenotype.getEvidenceLink().getDisplay()}">
                <c:if test='${phenotype.getEvidenceLink().getIconType().name().equalsIgnoreCase("IMAGE")}'>
                    <td data-sort="none">
                        <i class="fa fa-image" title="No images available."></i>
                    </td>
                </c:if>
                <c:if test='${phenotype.getEvidenceLink().getIconType().name().equalsIgnoreCase("GRAPH")}'>
                    <td data-sort="none">
                        <i class="fa fa-bar-chart-o" title="No supporting data supplied."></i>
                    </td>
                </c:if>


            </c:if>


            <!-- This is closing the td from the 2 ifs above -->

        </tr>
    </c:forEach>
    </tbody>
</table>

<!-- /row -->
