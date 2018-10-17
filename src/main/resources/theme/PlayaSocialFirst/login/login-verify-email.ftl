<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "title">
        ${msg("emailVerifyTitle")}
    <#elseif section = "header">
        ${msg("emailVerifyTitle")}
    <#elseif section = "form">
        <p class="instruction">
            ${msg("lblCheckEmail")}
            <clr-alert _ngcontent-c1="" clralerttype="danger">
                <div class="alert alert-danger">
                    <div class="alert-items">
                        <clr-alert-item _ngcontent-c1="" class="alert-item">
                            <div class="alert-icon-wrapper">
                                <clr-icon class="alert-icon" shape="exclamation-circle"><svg version="1.1" class="has-solid " viewBox="0 0 36 36" preserveAspectRatio="xMidYMid meet" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" focusable="false" aria-hidden="true" role="img"><path class="clr-i-outline clr-i-outline-path-1" d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm0,22A10,10,0,1,1,28,18,10,10,0,0,1,18,28Z"></path>
                                <path class="clr-i-outline clr-i-outline-path-2" d="M18,20.07a1.3,1.3,0,0,1-1.3-1.3v-6a1.3,1.3,0,1,1,2.6,0v6A1.3,1.3,0,0,1,18,20.07Z"></path>
                                <circle class="clr-i-outline clr-i-outline-path-3" cx="17.95" cy="23.02" r="1.5"></circle>
                                <path class="clr-i-solid clr-i-solid-path-1" d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm-1.49,6a1.49,1.49,0,0,1,3,0v6.89a1.49,1.49,0,1,1-3,0ZM18,25.5a1.72,1.72,0,1,1,1.72-1.72A1.72,1.72,0,0,1,18,25.5Z"></path></svg></clr-icon>
                            </div>
                            <span _ngcontent-c1="" class="alert-text">
                                ${msg("lblNotGetEmail")} <a style="display: inline; padding: 0;" href="${url.loginAction}">${msg("lblDoPush")}</a> ${msg("lblResendEmail")}
                            </span>
                        </clr-alert-item>
                    </div>
                </div>
            </clr-alert>
        </p>
    </#if>
</@layout.registrationLayout>