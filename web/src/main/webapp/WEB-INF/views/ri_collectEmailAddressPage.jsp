<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ page pageEncoding="UTF-8" %>
<%@ page isELIgnored="false"%>

<t:genericpage>

    <jsp:attribute name="title">${title}</jsp:attribute>

    <jsp:attribute name="header">
        <script src='https://www.google.com/recaptcha/api.js'></script>
    </jsp:attribute>

    <jsp:attribute name="bodyTag">
        <body class="no-sidebars small-header">
    </jsp:attribute>

    <jsp:attribute name="addToFooter"></jsp:attribute>

    <jsp:body>

        <jsp:useBean id="current" class="java.util.Date" />

        <div class="container data-heading">
            <div class="row row-shadow">

                <div class="col-12 no-gutters">
                    <h2 class="title">${title}</h2>
                </div>
            </div>
        </div>

        <div class="container single single--no-side">
            <div class="breadcrumbs" style="box-shadow: none; margin-top: auto; margin: auto; padding: auto">
                <div class="row">
                    <div class="col-md-12">
                        <p><a href="${baseUrl}">Home</a>
                            <span class="fal fa-angle-right"></span><a href="${baseUrl}/summary">My Genes</a>
                            <span class="fal fa-angle-right"></span> ${title}
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <div class="container single single--no-side">
            <div class="row row-over-shadow">
                <div class="col-md-12 white-bg">
                    <p>
                        Please enter the e-mail address for your account and press the <i>${title}</i> button. To
                        complete this operation, please reply to the e-mail sent to that address.
                    </p>

                    <div class="login-form row ml-0 mr-0 mb-2">
                        <form
                                class="col-md-6"
                                action="${baseUrl}/sendEmail"
                                method="POST">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <input type="hidden" name="action" value="${title}" />

                            <c:if test="${not empty param.error}">
                                <c:if test="${empty param.errorMessage}">
                                    <div class="alert alert-danger">
                                        <p>Invalid username and password.</p>
                                    </div>
                                </c:if>
                                <c:if test="${not empty param.errorMessage}">
                                    <div class="alert alert-danger">
                                        <p>${param.errorMessage}</p>
                                    </div>
                                </c:if>
                            </c:if>
                            <c:if test="${status != null}">
                                <div class="alert alert-success">
                                    <c:if test="${showWhen}">
                                        ${current.toLocaleString()}:&nbsp;
                                    </c:if>
                                    <p>${status}</p>
                                </div>
                            </c:if>

                            <div class="form-group row">
                                <label for="emailAddress" class="col-4 pl-0 col-form-label">Email address:</label>
                                <div class="col-6 m-0 pl-0">
                                    <input
                                            class="m-0 pl-1 col-md-12"
                                            type="email"
                                            class="form-control"
                                            id="emailAddress"
                                            name="emailAddress"
                                            value="${param.emailAddress}"
                                            placeholder="myaddress@example.com"
                                            required />
                                </div>
                            </div>

                            <div class="form-group row">
                                <label for="repeatEmailAddress" class="col-4 pl-0 col-form-label">Repeat email address:</label>
                                <div class="col-6 m-0 pl-0">
                                    <input
                                            class="m-0 pl-1 col-md-12"
                                            type="email"
                                            class="form-control"
                                            id="repeatEmailAddress"
                                            name="repeatEmailAddress"
                                            value="${param.repeatEmailAddress}"
                                            placeholder="myaddress@example.com"
                                            required />
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="pl-0">
                                    <div class="g-recaptcha" data-sitekey=${recaptchaPublic}></div>
                                </div>
                            </div>

                            <div class="form-group row">
                                <div class="col-5 pl-0">
                                    <input
                                            class="btn btn-block btn-primary btn-default"
                                            type="submit"
                                            value="${title}" />
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

    </jsp:body>
</t:genericpage>