<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<script>
    var orderContent = {};
    function detailFormatter(index, row) {
        if(!row._data['shown']) {
            $.ajax({
                url: row._data['link'],
                type: 'GET',
                success: function (data) {
                    $('#orderAllele' + index).html(data);
                    row._data['shown'] = true;
                    orderContent[index] = data;
                }
            });
            return "<div class='container'>" +
                '<div id="orderAllele' + index + '" class="col-12">' +
                "     <div class=\"pre-content\">\n" +
                "                        <div class=\"row no-gutters\">\n" +
                "                            <div class=\"col-12 my-5\">\n" +
                "                                <p class=\"h4 text-center text-justify\"><i class=\"fas fa-atom fa-spin\"></i> A moment please while we gather the data . . . .</p>\n" +
                "                            </div>\n" +
                "                        </div>\n" +
                "                    </div>" +
                '</div>' +
                '</div>';
        } else {
            return "<div class='container'>" +
                '<div id="orderAllele' + index + '" class="col-12">' +
                orderContent[index] +
                '</div>' +
                '</div>';
        }

    }
</script>

<c:if test="${orderRows.size() > 0}">
    <c:if test="${creLine}">
        <c:set var="creLineParam" value="&creLine=true"/>
    </c:if>
    <table id="creLineTable" data-toggle="table" data-pagination="true" data-mobile-responsive="true" data-sortable="true"   data-detail-view="true" data-detail-formatter="detailFormatter">
        <thead>
        <tr>
            <th>MGI Allele</th>
            <th>Allele Type</th>
            <th>Availability</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="row" items="${orderRows}" varStatus="status">
            <tr data-link="${baseUrl}/allelesFrag/${row.mgiAccessionId}/${row.encodedAlleleName}?${creLineParam}" data-shown="false">
                <td>
                    <span class="text-dark" style="font-size: larger; font-weight: bolder;">${row.markerSymbol}<sup>${row.alleleName}</sup></span>
                </td>
                <td>
                        ${row.alleleDescription}
                </td>
                <td>
                    <c:if test="${row.mouseAvailable}">
                        <span>
                            Mice<c:if test="${row.targetingVectorAvailable or row.esCellAvailable or row.tissuesAvailable}">,</c:if>
                        </span>

                    </c:if>
                    <c:if test="${row.targetingVectorAvailable}">
                        <span>Targeting vectors<c:if test="${row.esCellAvailable or row.tissuesAvailable}">,</c:if></span>
                    </c:if>
                    <c:if test="${row.esCellAvailable}">
                        <span>ES Cells<c:if test="${row.tissuesAvailable}">,</c:if></span>
                    </c:if>
                    <c:if test="${row.tissuesAvailable}">
                        <span>Tissue</span>
                    </c:if>

                    <%--c:if test="${row.tissuesAvailable}">
                        <c:forEach items="${row.getTissueTypes()}" var="item" varStatus="loop">
                            <a class="btn" href="${row.getTissueEnquiryLinks().get(loop.index)}"
                               style="margin-bottom: 0.25em;"><i class="fa fa-envelope"></i> ${item}</a><br>
                        </c:forEach>
                    </c:if--%>
                </td>
<%--                <td>
                    <a class="btn btn-outline-primary">Show ordering information &nbsp;<i class="fa fa-caret-down"></i></a>
                </td>--%>
            </tr>
        </c:forEach>
        </tbody>

    </table>
</c:if>


<c:choose>
    <c:when test="${creLineAvailable}">
        <div><a href="${baseUrl}/order/creline?acc=${acc}" target="_blank">Cre
            Knockin ${alleleProductsCre2.get("product_type")} are available for this gene.</a></div>
    </c:when>
</c:choose>
                            	
                            	