<#import "template.ftl" as layout>
<@layout.registrationLayout isMainRegistration=true; section>
    <#if section = "title">
        ${msg("registerWithTitle",(realm.displayName!''))}
    <#elseif section = "header">
        ${msg("registerWithTitleHtml",(realm.displayNameHtml!''))?no_esc}
    <#elseif section = "form">
        <div class="modal-shadow register-shadow"   style="display:block; id="modal-shadow"></div>
        <div class="modal request-modal register-modal " id="modalwindow" >
            <div class="">
                <div class="modal-dialog" role="dialog" aria-hidden="true">
                    <div class="modal-content">
                        <div class="modal-header">
                            <a class="close close_ccs" aria-label="Close" href="${url.loginUrl}"></a>
                            <h3 class="modal-title">${msg("lblRegistration")}</h3>
                        </div>
                        <div class="modal-body">
                            <form action="${url.registrationAction}" method="post" onchange="onchangef()" onsubmit="onsubmitf();">
                                <section class="form-block">
                                    <div class="form-group form-group form-group-request">
                                        <label for="rfirstName">${msg("firstName")}</label>
                                        <input type="text" id="rfirstName" name = "firstName"  value="${(register.formData.firstName!'')}" placeholder="" size="45">
                                    </div>
                                    <input type="hidden" id="lastName" name="lastName" value="5f1b81b8-4f5d-11e8-9c2d-fa7ae01bbebc">
                                    <input type="hidden" id="username" name="username" value="${(register.formData.email!'')}">
                                    <div class="form-group form-group form-group-request">
                                        <label for="remail">${msg("lblEmailOrUsername")}</label>
                                        <input type="text" id="remail" name = "email" value="${(register.formData.email!'')}" placeholder="" size="45" />
                                    </div>
                                    <div class="form-group form-group form-group-request">
                                        <label for="rpassword">${msg("password")}</label>
                                        <input id="rpassword" size="45" placeholder="" class="" name="password" type="password">
                                        <div id="showpass" class="showpass_register" onclick="ShowHidePassword('rpassword')"></div>
                                    </div>
                                    <input type="hidden" id="rpassword-confirm" name = "password-confirm" placeholder="" size="45">
                                    <#--<div class="checkbox checkbox-request">-->
                                        <#--<input type="checkbox" onclick="checkcheckbox(this)" id="checkrads_1">-->
                                        <#--<label for="checkrads_1">${msg("lblPrivacyPolicy")} <nobr><a target="_blank" href="https://scf.playa.ru/agreement.html">${msg("lblPrivacyPolicyAgreement")}</a></nobr></label>-->
                                    <#--</div>-->
                                    <#if message?has_content>
                                        <div class="${properties.kcFeedbackAreaClass!}" style="margin-bottom: 20px;">
                                            <#if message.type = 'success'>
                                                <div class="alert alert-${message.type}">
                                                    <span class="${properties.kcFeedbackSuccessIcon!}"></span>
                                                    <span class="kc-feedback-text">${message.summary?no_esc}</span>
                                                </div>
                                            </#if>
                                            <#if message.type = 'warning'>
                                                <div class="alert alert-${message.type}">
                                                    <span class="${properties.kcFeedbackWarningIcon!}"></span>
                                                    <span class="kc-feedback-text">${message.summary?no_esc}</span>
                                                </div>
                                            </#if>
                                            <#if message.type = 'error'>
                                                <div class="alert alert-danger">
                                                    <clr-alert _ngcontent-c1="" clralerttype="danger">
                                                        <div class="alert-items">
                                                            <clr-alert-item _ngcontent-c1="" class="alert-item">
                                                                <div class="alert-icon-wrapper">
                                                                    <clr-icon class="alert-icon" shape="exclamation-circle"><svg version="1.1" class="has-solid " viewBox="0 0 36 36" preserveAspectRatio="xMidYMid meet" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" focusable="false" aria-hidden="true" role="img"><path class="clr-i-outline clr-i-outline-path-1" d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm0,22A10,10,0,1,1,28,18,10,10,0,0,1,18,28Z"></path>
                                                                    <path class="clr-i-outline clr-i-outline-path-2" d="M18,20.07a1.3,1.3,0,0,1-1.3-1.3v-6a1.3,1.3,0,1,1,2.6,0v6A1.3,1.3,0,0,1,18,20.07Z"></path>
                                                                    <circle class="clr-i-outline clr-i-outline-path-3" cx="17.95" cy="23.02" r="1.5"></circle>
                                                                    <path class="clr-i-solid clr-i-solid-path-1" d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm-1.49,6a1.49,1.49,0,0,1,3,0v6.89a1.49,1.49,0,1,1-3,0ZM18,25.5a1.72,1.72,0,1,1,1.72-1.72A1.72,1.72,0,0,1,18,25.5Z"></path></svg></clr-icon>
                                                                </div>
                                                                <span _ngcontent-c1="" class="alert-text">
                                                                    ${message.summary?no_esc}
                                                                    <#if message.summary = msg("emailExistsMessage")>
                                                                        ${msg("lblAuthText")} <a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a> ${msg("lblForResetPassword")}
                                                                    </#if>
                                                                </span>
                                                            </clr-alert-item>
                                                        </div>
                                                    </clr-alert>
                                                </div>
                                            </#if>
                                            <#if message.type = 'info'>
                                                <div class="alert alert-${message.type}">
                                                    <span class="${properties.kcFeedbackInfoIcon!}"></span>
                                                    <span class="kc-feedback-text">${message.summary?no_esc}</span>
                                                </div>
                                            </#if>
                                        </div>
                                    </#if>
                                    <div class="btn-group btn-primary btn-request reg_form">
                                        <input class="btn btn-primary width-200px" name="register" type="submit" value="${msg("doRegister")}">
                                        <span style="padding-left: 20px"><a style="display: inline; padding: 0;" tabindex="5" href="${url.loginUrl}">${msg("doCancel")}</a></span>
                                    </div>
                                </section>
                                <script>
                                    if(document.getElementById('remail').value === '' && getCookie('email')){
                                        document.getElementById('remail').value = getCookie('email');
                                    }

                                    function onsubmitf() {
                                        setCookie('email', document.getElementById('remail').value, {expires: 300})

                                        document.getElementById('username').value = document.getElementById('remail').value;
                                        document.getElementById('rpassword-confirm').value = document.getElementById('rpassword').value;

                                        return true;
                                    }

                                    function onchangef() {
                                        document.getElementById('username').value = document.getElementById('remail').value;
                                        document.getElementById('rpassword-confirm').value = document.getElementById('rpassword').value;
                                    }

                                    function ShowHidePassword(id) {
                                        element = document.getElementById(id);
                                        imgelement=document.getElementById('showpass');
                                        if (element.type === 'password') {element.type = 'text';imgelement.setAttribute("class", "showpass_no_register");}
                                        else {element.type = 'password';imgelement.setAttribute("class", "showpass_register");}
                                        document.baseform.password.focus();
                                    }
                                </script>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </#if>
</@layout.registrationLayout>