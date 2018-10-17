<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>

    <#if section = "title">
        ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
        ${msg("loginTitleHtml",(realm.displayNameHtml!''))?no_esc}
    <#elseif section = "form">
        <#if realm.password>
            <form id="kc-form-login " class="${properties.kcFormClass!} authorize-form" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="${properties.kcFormGroupClass!}">
                    <div class="width-320px">
                        <div id="part1">
                            <div class="group-title">${msg("lblSocialEnter")}</div>
                            <div class="form-group form-group-login-text column-container">
                                <div id="kc-info" class="">
                                    <div id="kc-info-wrapper" class="">
                                        <div id="kc-social-providers">
                                            <ul>
                                                <#if social.providers?has_content>
                                                    <#list social.providers as p>
                                                        <li>
                                                            <div class="login_social">
                                                                <a href="${p.loginUrl}" id="zocial-${p.alias}" onclick="save('${url.loginUrl}')">
                                                                    <img src="${url.resourcesPath}/img/ico_${p.alias}.png">
                                                                </a>
                                                            </div>
                                                        </li>
                                                    </#list>
                                                </#if>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="part2">
                            <div class="group-title">
                                ${msg("lblIfAlreadyRegister")}
                                <div class="link_css" onclick="show()">${msg("lblIfAlreadyRegisterEnter")}</div>
                            </div>
                            <div id="oauth" class="hide_css">
                                <div class="form-group form-group-login">
                                    <#if usernameEditDisabled??>
                                        <input tabindex="1" id="username" placeholder="${msg("lblEmailOrUsername")}" name="username" value="${(login.username!'')}" size="45" type="text" disabled />
                                    <#else>
                                        <input tabindex="1" id="username" placeholder="${msg("lblEmailOrUsername")}" name="username" value="${(login.username!'')}" size="45" type="text" autocomplete="off" />
                                    </#if>
                                </div>
                                <div class="${properties.kcFormGroupClass!}">
                                    <div class="form-group form-group-login">
                                        <input tabindex="2" id="password" size="45" placeholder="${msg("password")}" class="" name="password" type="password" autocomplete="off">
                                        <div id=showpass class=showpass onclick="ShowHidePassword('password')"></div>
                                    </div>
                                </div>
                                <div class="${properties.kcFormGroupClass!}">
                                    <div id="kc-form-buttons" class="btn-group btn-primary btn-request">
                                        <input class="btn btn-primary" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}">
                                        <span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div id="part3">
                            <div class="group-title">${msg("lblOrYouCan")} <div class="link_css" onclick="window.location.href='${url.registrationUrl}'; deleteCookie('email'); return true;">${msg("lblProcedureOfRegistration")}</div></div>
                        </div>
                    </div>
                </div>
            </form>

            <script type="text/javascript">
                function save(s) {
                    window.localStorage.setItem("url_path", s);
                }

                function ShowHidePassword(id) {
                    element = document.getElementById(id);
                    imgelement=document.getElementById('showpass');
                    if (element.type === 'password') {element.type = 'text';imgelement.setAttribute("class", "showpass_no");}
                    else {element.type = 'password';imgelement.setAttribute("class", "showpass");}
                    document.baseform.password.focus();
                }

                function setCookie(name, value, options) {
                    console.log("setCookie: " + value);
                    options = options || {};

                    var expires = options.expires;

                    if (typeof expires === "number" && expires) {
                        var d = new Date();
                        d.setTime(d.getTime() + expires * 1000);
                        expires = options.expires = d;
                    }
                    if (expires && expires.toUTCString) {
                        options.expires = expires.toUTCString();
                    }

                    value = encodeURIComponent(value);

                    var updatedCookie = name + "=" + value;

                    for (var propName in options) {
                        updatedCookie += "; " + propName;
                        var propValue = options[propName];
                        if (propValue !== true) {
                            updatedCookie += "=" + propValue;
                        }
                    }

                    document.cookie = updatedCookie;
                }
                function deleteCookie(name) {
                    setCookie(name, "", {
                        expires: -1
                    })
                }

                function show() {
                    document.getElementById('oauth').className = 'show_css'
                }
            </script>
        </#if>
    <#elseif section = "info" >

    </#if>
</@layout.registrationLayout>
