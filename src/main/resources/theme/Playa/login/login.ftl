<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>

    <#if section = "title">
        ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))?no_esc}
    <#elseif section = "form">
        <#if realm.password>
            <form name="baseform" id="kc-form-login " class="authorize-form" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="form-group form-group-login">
                    <input tabindex="1" id="username" placeholder="${msg("lblEmailOrUsername")}" name="username" value="" size="45" type="text" autofocus="" autocomplete="off">
                </div>

                <div class="form-group form-group-login">
                    <input tabindex="2" id="password" size="45" placeholder="${msg("password")}" class="" name="password" type="password" autocomplete="off">
                    <div id=showpass class=showpass onclick="ShowHidePassword('password')"></div>
                </div>

                <div id="kc-form-buttons" class="btn-primary btn-request">
                    <input class="btn btn-primary" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}">
                    <a class="formlink" tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a> | <a class="formlink" tabindex="5" href="${url.registrationUrl}">${msg("doRegister")}</a>
                </div>

                <div class="form-group form-group-login-text column-container">
                    <div class="group-title">${msg("lblOrAuthSocial")}
                    </div>
                    <div id="kc-social-providers">
                        <ul>
                            <#if social.providers?has_content>
                                <#list social.providers as p>
                                    <li>
                                        <div  class="login_social">
                                            <a href="${p.loginUrl}" id="zocial-${p.alias}" onclick="save('${url.loginUrl}')">
                                                    <img src="${url.resourcesPath}/img/ico_${p.alias}.png"/>
                                            </a>
                                        </div>
                                    </li>
                                </#list>
                            </#if>
                        </ul>
                    </div>
                </div>
            </form>

            <script type="text/javascript">
                function save(s) {
                    window.localStorage.setItem("url_path", s);
                }

                function ShowHidePassword(id) {
                    element = document.getElementById(id);
                    imgelement = document.getElementById('showpass');
                    if (element.type === 'password') {
                        element.type = 'text';
                        imgelement.setAttribute("class", "showpass_no");
                    } else {
                        element.type = 'password';
                        imgelement.setAttribute("class", "showpass");
                    }
                    document.baseform.password.focus();
                }
            </script>
        </#if>
    <#elseif section = "info" >

    </#if>
</@layout.registrationLayout>
