<%--
  Created by IntelliJ IDEA.
  User: ckc
  Date: 15/02/2016
  Time: 16:41
  To change this template use File | Settings | File Templates.
--%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<c:set var="expressionIcon" scope="page" value="fa fa-check"/>
<c:set var="noTissueIcon" scope="page" value="fa fa-circle-o"/>
<c:set var="noExpressionIcon" scope="page" value="fa fa-times"/>
<c:set var="ambiguousIcon" scope="page" value="fa fa-circle"/>
<c:set var="yesColor" scope="page" value="text-info"/>
<c:set var="noColor" scope="page" value="text-muted"/>
<div class="container">
    <div class="row justify-content-center">
        <span title="Expression" class="${yesColor} mr-3"><i class="${expressionIcon}"></i> &nbsp;Expression</span>
        <span title="No Expression" class="${noColor} mr-3"> <i class="${noExpressionIcon}"></i>&nbsp;No Expression</span>
        <span title="No Tissue Available" class="${noColor} mr-3"><i class="${noTissueIcon}"></i>&nbsp;No Tissue Available</span>
        <span title="Ambiguous" class="${noColor}"><i class="${ambiguousIcon}"></i>&nbsp;Ambiguous</span>
    </div>
    <div class="row justify-content-center">
        <div class="col-md-10">
            <div id="toggleWt" class="float-right">Show Wildtype Expression</div>
        </div>
    </div>


    <script>
        $(document).ready(function () {
            $('#expressionTable').DataTable({responsive: true});
        });
    </script>

    <!-- <h2 class="title" id="section-impc_expression">Expression Overview<i class="fa fa-question-circle pull-right" title="Brief info about this panel"></i></h2>
    -->
    <div class="row justify-content-center">
        <div class="col-md-10">
            <table id="expressionTable" class="table table-striped table-bordered dt-responsive" style="width:100%">
                <thead>
                <th>Anatomy</th>
                <th
                        title="Number of heterozygous mutant specimens with data for the specified anatomy">
                    #HET Specimens
                </th>
                <th
                        title="If there are images for homozygous specimens this value will be 'Yes'">
                    HOM Images?
                </th>
                <th class="wtExp"
                    title="Status of expression for Wild Type specimens from any colony with data for this anatomy">
                    WT Expr
                </th>
                <th title="">Mutant Expr</th>
                <%-- <th>Mutant specimens</th> --%>
                <th
                        title="An clickable image icon will show if images are available for mutant specimens">
                    Images
                </th>
                </thead>
                <tbody>
                <c:forEach var="mapEntry"
                           items="${expressionAnatomyToRow}">
                    <tr>
                        <td><a
                                href="${baseUrl}/anatomy/${mapEntry.value.abnormalAnatomyId}">${mapEntry.value.abnormalAnatomyName}</a>
                        </td>
                        <td><span
                                title="${mapEntry.value.numberOfHetSpecimens} Heterozygous Mutant Mice">${mapEntry.value.numberOfHetSpecimens}</span>
                        </td>
                        <td
                                <c:if test="${mutantImagesAnatomyToRow[mapEntry.key].homImages}">style="color:${yesColor}"</c:if>>
                                     			<span
                                                        title="Homozygote Images are
                                     			<c:if test="${!mutantImagesAnatomyToRow[mapEntry.key].homImages}">not</c:if> available"><c:if
                                                        test="${mutantImagesAnatomyToRow[mapEntry.key].homImages}">Yes</c:if>
																<c:if
                                                                        test="${!mutantImagesAnatomyToRow[mapEntry.key].homImages}">No</c:if></span>
                        </td>
                        <td class="wtExp">
                            <c:choose>
                                <c:when
                                        test="${wtAnatomyToRow[mapEntry.key].expression}">
                                    <i
                                            title="WT Expressed: ${fn:length(wtAnatomyToRow[mapEntry.key].specimenExpressed)} wild type specimens expressed from a total of ${fn:length(wtAnatomyToRow[mapEntry.key].specimen)} wild type specimens"
                                            class="${expressionIcon} ${yesColor}"
                                            style=""></i>(${fn:length(wtAnatomyToRow[mapEntry.key].specimenExpressed)}/${fn:length(wtAnatomyToRow[mapEntry.key].specimen)})
                                </c:when>
                                <c:when
                                        test="${wtAnatomyToRow[mapEntry.key].notExpressed}">
                                    <i
                                            title="WT NOT expressed: ${fn:length(wtAnatomyToRow[mapEntry.key].specimenNotExpressed)} Not Expressed ${fn:length(wtAnatomyToRow[mapEntry.key].specimen)} wild type specimens"
                                            class="${noExpressionIcon} color:${noColor}"></i>
                                </c:when>
                                <c:when
                                        test="${wtAnatomyToRow[mapEntry.key].noTissueAvailable}">
                                    <i title="WT No Tissue Available"
                                       class="${noTissueIcon}"
                                       style="color:${noColor}"></i>
                                </c:when>

                                <c:otherwise>
                                     				<span title="Ambiguous"
                                                          class="${ambiguousIcon}" style="color:${noColor}"></span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <c:choose>
                                <c:when
                                        test="${mapEntry.value.expression}">
                                     				<span
                                                            title="Expressed: ${fn:length(mapEntry.value.specimenExpressed)} mutant specimens expressed from a total of ${fn:length(mapEntry.value.specimen)} mutant specimens"
                                                            class="${expressionIcon}"
                                                            style="color:${yesColor}"></span>(${fn:length(mapEntry.value.specimenExpressed)}/${fn:length(mapEntry.value.specimen)})
                                </c:when>
                                <c:when
                                        test="${mapEntry.value.notExpressed}">
                          							<span
                                                            title="Not Expressed: ${fn:length(mapEntry.value.specimenNotExpressed)} Not Expressed from a total of ${fn:length(mapEntry.value.specimen)} mutant specimens"
                                                            class="${noExpressionIcon}" style="color:${noColor}"></span>
                                </c:when>
                                <c:when
                                        test="${mapEntry.value.noTissueAvailable}">
                          							<span title="No Tissue Available"
                                                          class="${noTissueIcon}" style="color:${noColor}"></span>
                                </c:when>

                                <c:otherwise>
                                     				<span title="Ambiguous"
                                                          class="${ambiguousIcon}" style="color:${noColor}"></span>
                                </c:otherwise>
                            </c:choose>
                        </td>

                            <%-- <td>
                            <c:forEach var="specimen" items="${mapEntry.value.specimen}">
                            <i title="zygosity= ${specimen.value.zyg}">${specimen.key}</i>
                            </c:forEach></td> --%>

                        <td>
                            <c:if
                                    test="${mutantImagesAnatomyToRow[mapEntry.key].wholemountImagesAvailable}">
                                <!-- imageComparator?acc=MGI:1859162&anatomy_term=respiratory%20system&parameter_stable_id=IMPC_ALZ_075_001 -->
                                <a
                                        href='${baseUrl}/imageComparator?acc=${acc}&anatomy_id=${mapEntry.value.abnormalAnatomyId}&parameter_stable_id=IMPC_ALZ_076_001'><i
                                        title="Wholemount Images available (click on this icon to view images)"
                                        class="fa fa-image"
                                        alt="Images"><%-- (${mutantImagesAnatomyToRow[mapEntry.key].numberOfImages}) --%></i>
                                </a>
                            </c:if>
                            <c:if
                                    test="${mutantImagesAnatomyToRow[mapEntry.key].sectionImagesAvailable}">
                                <a
                                        href='${baseUrl}/imageComparator?acc=${acc}&anatomy_id=${mapEntry.value.abnormalAnatomyId}&parameter_stable_id=IMPC_ALZ_075_001'><i
                                        title="Section Images available (click on this icon to view images)"
                                        class="fa fa-image"
                                        alt="Images"><%-- (${mutantImagesAnatomyToRow[mapEntry.key].numberOfImages}) --%></i>
                                </a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

</div>

