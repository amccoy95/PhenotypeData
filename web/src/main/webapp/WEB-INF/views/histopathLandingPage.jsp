<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:genericpage-histopath>

    <jsp:attribute name="title">Histopath Landing Page</jsp:attribute>
    <jsp:attribute name="header">

<%--        <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/fixedcolumns/3.3.0/css/fixedColumns.dataTables.min.css"/>--%>
<%--        <script type="text/javascript" src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"/>--%>
<%--        <script type="text/javascript" src="https://cdn.datatables.net/fixedcolumns/3.3.0/js/dataTables.fixedColumns.min.js"/>--%>


    </jsp:attribute>

    <jsp:attribute name="addToFooter">
<style type="text/css" media="all">

        table{
  margin: 0 auto;
  width: 100%;
  clear: both;
  border-collapse: collapse;
  table-layout: fixed; // ***********add this
  word-wrap:break-word; // ***********and this
}

</style>

        <script>

            $(document).ready( function () {
                console.log('documen is ready');
                var table = $('#heatmap').DataTable({
                    "pageLength": 100,
                    scrollY:        500,
                    scrollX:        true,
                    fixedHeader: true,
                    //scrollCollapse: true,
                    paging:         true,
                    fixedColumns:   true,
                    'createdRow': function(row, data, index){
                       // $(row).find('td:eq(1)').css('background-color', 'Orange');

                        for(var i=1; i<data.length; i++) {
                            if (data[i] == 0) {
                                $(row).find('td:eq('+i+')').css('color', 'rgba(0, 0, 0, 0.0)');
                            }else
                            if (data[i] == 1) {
                                $(row).find('td:eq('+i+')').css('background-color', '#808080').css('color', 'rgba(0, 0, 0, 0.0)').css("pointer-events", "none");
                            }else
                            if (data[i] == 2) {
                                $(row).find('td:eq('+i+')').css('background-color', '#17a2b8').css('color', 'rgba(0, 0, 0, 0.0)').css('cursor','pointer');
                            }else
                            if (data[i] == 4) {
                                $(row).find('td:eq('+i+')').css('background-color', '#ce6211').css('color', 'rgba(0, 0, 0, 0.0)').css('cursor','pointer');
                            }
                        }
                    },
                });


                <%--$('#heatmap tbody').on('click', 'tr', function () {--%>
                <%--    var url='${baseUrl}/histopath/';--%>
                <%--    console.log('colIndex='+table.row( this ).colIndex+' rowIndex='+this.parentNode.rowIndex);--%>
                <%--    var data = table.row( this ).data();--%>
                <%--    url=url+data[0];--%>
                <%--    //var win = window.open(url, '_blank');--%>
                <%--    //win.focus();--%>
                <%--} );--%>

                <%--$('#heatmap tbody').on( 'click', 'td', function () {--%>
                <%--    var url='${baseUrl}/histopath/';--%>
                <%--    var idx = table.cell( this ).index().column;--%>
                <%--    var idxRow = table.row( this ).index().row;--%>
                <%--    var title = table.column( idx ).header();--%>
                <%--    url=url+'#'+$(title).text();--%>
                <%--    alert( 'title='+title+'Column title clicked on: col index '+idx+' row index='+idxRow );--%>
                <%--} );--%>

                table.on('click', 'tbody td', function() {
                    var url='${baseUrl}/histopath/';
                    //get textContent of the TD
                    var anatomy = table.column( this.cellIndex ).header();
                    var row_data=table.row(this).data();
                    var gene_symbol=row_data[0];
                    //get the value of the TD using the API
                    url=url+gene_symbol;
                    if($(anatomy).text()){
                        url=url+'?anatomy="'+$(anatomy).text()+'"';
                    }
                    console.log('url='+url);
                    var win = window.open(url, '_blank');
                    win.focus();
                })


                });


        </script>

    </jsp:attribute>

    <jsp:body>
        <div class="container single single--no-side">

            <div class="breadcrumbs" style="box-shadow: none; margin-top: auto; margin: auto; padding: auto">

                <div class="row">
                    <div class="col-md-12">
                        <p><a href="/">Home</a>
                            <span class="fal fa-angle-right"></span> Histopathology
                        </p>
                    </div>
                </div>
            </div>

            <div class="row row-over-shadow">
                <div class="col-md-12 white-bg">
                    <div class="page-content">

                        <div class="card">
                            <div class="card-header">Histopathology for every gene tested</div>
                            <div class="card-body">
                                <p class="my-0"><b>Significance Score:</b></p>
                                <c:set var="noData" scope="page" value="fa fa-circle"/>
                                <c:set var="notApplicable" scope="page" value="fa fa-circle"/>
                                <c:set var="notSignificant" scope="page" value="fa fa-circle"/>
                                <c:set var="significant" scope="page" value="fa fa-circle"/>
                                <c:set var="noDataColour" scope="page" value="#fff"/>
                                <c:set var="notApplicableColour" scope="page" value="#808080"/>
                                <c:set var="notSignificantColour" scope="page" value="#17a2b8"/>
                                <c:set var="significantColour" scope="page" value="#ce6211"/>
                                <div style="background-color: whitesmoke">
                                    <div title="No Data"  class="mr-3"><i class="${noData}" style="color: white"></i>&nbsp;&nbsp;No Data</div>
                                    <div title="Not Applicable" style="color: ${notApplicableColour}" class="mr-3"> <i class="${notApplicable}"></i>&nbsp;&nbsp;Not Applicable</div>
                                    <div title="Not Signficant" style="color: ${notSignificantColour}" class="mr-3"><i class="${notSignificant}"></i>&nbsp;&nbsp;<b>Not Significant</b> (histopathology finding that is interpreted by the
                                        histopathologist to be within normal limits of background strain-related
                                        findings or an incidental finding not related to genotype)</div>
                                    <div title="Significant" style="color: ${significantColour}" class="mr-3"><i class="${significant}"></i>&nbsp;&nbsp;<b>Significant</b> (histopathology finding that is interpreted by the
                                        histopathologist to not be a background strain-related finding or an incidental
                                        finding)</div>
                                </div>
                            </div>
                        </div>
                        <br/>
                        <br/>

                            <table id="heatmap" class="display cell-border compact row-border" style="font-size: 10px">
                                <thead>
                                <tr>
                                    <th><div class="rotate90">Gene</div></th>
                                    <c:forEach items="${anatomyHeaders}" var="parameter">
                                        <th><div class="rotate90">${parameter}</div></th>
                                    </c:forEach>

                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="arow" items="${rows}" varStatus="status">
                                    <tr>
                                        <td>${geneSymbols[status.index]}</td>
                                            <c:forEach var="acolumn" items="${arow}">
                                                <td>${acolumn}</td>
                                            </c:forEach>
                                    </tr>
                                </c:forEach>
                                </tbody>
                                <tfoot>
                                <tr>
                                    <th><div class="rotate90">Gene</div></th>
                                    <c:forEach var="parameter" items="${anatomyHeaders}">
                                        <th><div class="rotate90">${parameter}</div></th>
                                    </c:forEach>
                                </tr>
                                </tfoot>
                            </table>



                    </div>
                </div>
            </div>





        </div>

    </jsp:body>


    </t:genericpage-histopath>

