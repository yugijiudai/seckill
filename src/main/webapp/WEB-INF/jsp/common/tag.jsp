<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--<c:set var="ctx" value="${pageContext.request.contextPath}"/>--%>
<%
    pageContext.setAttribute("ctx", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath());
%>
<script>
    const ctx = '${ctx}';
</script>





