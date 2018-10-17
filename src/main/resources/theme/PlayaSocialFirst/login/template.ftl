<#macro registrationLayout bodyClass="" isMainRegistration=false displayInfo=false displayMessage=true>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="${properties.kcHtmlClass!}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <script>
        function checkcheckbox(val) {
            var sbmt = document.getElementById("chkbtn");
            if (val.checked == true) {
                sbmt.disabled = false;
            } else {
                sbmt.disabled = true;
            }
        }

        function getCookie(name) {
            var matches = document.cookie.match(new RegExp(
                    "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
            ));
            return matches ? decodeURIComponent(matches[1]) : undefined;
        }
        function setCookie(name, value, options) {
            console.log("setCookie: " + value);
            options = options || {};

            var expires = options.expires;

            if (typeof expires == "number" && expires) {
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

    </script>
    <title><#nested "title"></title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />

        </#list>
    </#if>

    <link href="${url.resourcesPath}/css/clr_style.css" rel="stylesheet" />

    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>

    <style>
        .hide{transform-origin: 0 0; opacity:0;  transform: scale(0);  transition: .4s ease-in-out; height:1px; }
        .part2:hover .hide{  transform: scale(1); transform-origin: 0 0; opacity:1; height:150px;}
        .part1{opacity:1;}
        .part2{opacity:1; margin-top:30px; margin-bottom:0px; }
        .part3{opavity:1; padding-top:30px;}
        .part3 .group-title{ font-weight:500;}
        .group-title{font-size:13px; font-family: "Trebuchet MS", sans-serif; color:#555; display;block; font-weight:600;}
        .link{padding-top:30px; cursor:pointer; text-decoration:underline; color:#007cbb; display:inline;}
        a{font-size:13px; font-family: "Trebuchet MS", sans-serif; }
        span a{padding-top:10px; padding-left:30px; display:block;}
        .center-container{display;block; position:absolute; }
        .addon{padding-left:25px;}
        .form-group-login-text{margin-left:0px;}
        .btn{height:40px; width:150px; border-top-right-radius:3px!important; border-bottom-right-radius:3px!imporant;  margin-top:3px;    line-height: 1.4rem; margin-right:30px;}
        .form-block{height:500px;}
    </style>
    <script>
        function shadow(i) {
            if (i == 1) {document.getElementById('part1').style.opacity=0.2; document.getElementById('part3').style.opacity=0.2;}
            if (i == 2) {document.getElementById('part1').style.opacity=1; document.getElementById('part3').style.opacity=1;}
            if (i == 3) {document.getElementById('part2').style.opacity=0.2; document.getElementById('part3').style.opacity=0.2;}
            if (i == 4) {document.getElementById('part2').style.opacity=1; document.getElementById('part3').style.opacity=1;}
            if (i == 5) {document.getElementById('part1').style.opacity=0.2; document.getElementById('part2').style.opacity=0.2;}
            if (i == 6) {document.getElementById('part1').style.opacity=1; document.getElementById('part2').style.opacity=1;}
        }
    </script>
</head>

<body class="${properties.kcBodyClass!} center-container">
<div>

    <#if isMainRegistration==true>
        <#nested "form">
    </#if>
    <section class = "form-block">
        <!-- <div id="kc-logo"><a href="${properties.kcLogoLink!'#'}"><div id="kc-logo-wrapper"></div></a></div> -->
        <div class="login-logo"></div>


    <#if displayMessage && message?has_content>
    <div class="${properties.kcFeedbackAreaClass!}">
        <#if message.summary != msg("verifyEmailMessage")>
                <#if message.type = 'success'>
                    <div class="alert alert-success">
                    <clr-alert _ngcontent-c1="" clralerttype="success">
                        <div class="alert-items">
                            <clr-alert-item _ngcontent-c1="" class="alert-item">
                                <div class="alert-icon-wrapper">
                                    <clr-icon class="alert-icon" shape="check-circle"><svg version="1.1" class="has-solid " viewBox="0 0 36 36" preserveAspectRatio="xMidYMid meet" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" focusable="false" aria-hidden="true" role="img"><path class="clr-i-outline clr-i-outline-path-1" d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm0,22A10,10,0,1,1,28,18,10,10,0,0,1,18,28Z"></path>
                                        <path class="clr-i-outline clr-i-outline-path-2" d="M16.34,23.74l-5-5a1,1,0,0,1,1.41-1.41l3.59,3.59,6.78-6.78a1,1,0,0,1,1.41,1.41Z"></path>
                                        <path class="clr-i-solid clr-i-solid-path-1" d="M30,18A12,12,0,1,1,18,6,12,12,0,0,1,30,18Zm-4.77-2.16a1.4,1.4,0,0,0-2-2l-6.77,6.77L13,17.16a1.4,1.4,0,0,0-2,2l5.45,5.45Z"></path></svg></clr-icon>
                                </div>

                                <span _ngcontent-c1="" class="alert-text">${message.summary?no_esc}</span>
                            </clr-alert-item>

                        </div>
                        <!---->
                    </clr-alert>
                    </div>
                </#if>
                <#if message.type = 'warning'>
                    <div class="alert alert-warning">
                        <clr-alert _ngcontent-c1="" clralerttype="warning">

                        <div class="alert-items">

                            <clr-alert-item _ngcontent-c1="" class="alert-item">
                                <div class="alert-icon-wrapper">
                                    <clr-icon class="alert-icon" shape="exclamation-triangle"><svg version="1.1" class="has-solid " viewBox="0 0 36 36" preserveAspectRatio="xMidYMid meet" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" focusable="false" aria-hidden="true" role="img"><path class="clr-i-outline clr-i-outline-path-1" d="M18,21.32a1.3,1.3,0,0,0,1.3-1.3V14a1.3,1.3,0,1,0-2.6,0v6A1.3,1.3,0,0,0,18,21.32Z"></path>
                                        <circle class="clr-i-outline clr-i-outline-path-2" cx="17.95" cy="24.27" r="1.5"></circle>
                                        <path class="clr-i-outline clr-i-outline-path-3" d="M30.33,25.54,20.59,7.6a3,3,0,0,0-5.27,0L5.57,25.54A3,3,0,0,0,8.21,30H27.69a3,3,0,0,0,2.64-4.43Zm-1.78,1.94a1,1,0,0,1-.86.49H8.21a1,1,0,0,1-.88-1.48L17.07,8.55a1,1,0,0,1,1.76,0l9.74,17.94A1,1,0,0,1,28.55,27.48Z"></path>
                                        <path class="clr-i-solid clr-i-solid-path-1" d="M30.33,25.54,20.59,7.6a3,3,0,0,0-5.27,0L5.57,25.54A3,3,0,0,0,8.21,30H27.69a3,3,0,0,0,2.64-4.43ZM16.46,12.74a1.49,1.49,0,0,1,3,0v6.89a1.49,1.49,0,1,1-3,0ZM18,26.25a1.72,1.72,0,1,1,1.72-1.72A1.72,1.72,0,0,1,18,26.25Z"></path></svg></clr-icon>
                                </div>

                                <span _ngcontent-c1="" class="alert-text">${message.summary?no_esc}
                                            </span>

                            </clr-alert-item>

                        </div>
                        </clr-alert>
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
                                    <!---->
                                    <span _ngcontent-c1="" class="alert-text">${message.summary?no_esc} </span>
                                </clr-alert-item>
                            </div>
                    </clr-alert>
                </div>
                </#if>
                <#if message.type = 'info'>
                    <div class="alert alert-info">
                    <clr-alert _ngcontent-c1="" clralerttype="info">
                        <div class="alert-items">
                            <clr-alert-item _ngcontent-c1="" class="alert-item">
                                <div class="alert-icon-wrapper">
                                    <clr-icon class="alert-icon" shape="info-circle"><svg version="1.1" class="has-solid " viewBox="0 0 36 36" preserveAspectRatio="xMidYMid meet" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" focusable="false" aria-hidden="true" role="img"><circle class="clr-i-outline clr-i-outline-path-1" cx="17.93" cy="11.9" r="1.4"></circle>
                                        <path class="clr-i-outline clr-i-outline-path-2" d="M21,23H19V15H16a1,1,0,0,0,0,2h1v6H15a1,1,0,1,0,0,2h6a1,1,0,0,0,0-2Z"></path>
                                        <path class="clr-i-outline clr-i-outline-path-3" d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm0,22A10,10,0,1,1,28,18,10,10,0,0,1,18,28Z"></path>
                                        <path class="clr-i-solid clr-i-solid-path-1" d="M18,6A12,12,0,1,0,30,18,12,12,0,0,0,18,6Zm-2,5.15a2,2,0,1,1,2,2A2,2,0,0,1,15.9,11.15ZM23,24a1,1,0,0,1-1,1H15a1,1,0,1,1,0-2h2V17H16a1,1,0,0,1,0-2h4v8h2A1,1,0,0,1,23,24Z"></path></svg></clr-icon>
                                </div>
                                <span _ngcontent-c1="" class="alert-text">${message.summary?no_esc} </span>

                            </clr-alert-item>

                        </div>
                        <!---->
                    </clr-alert>
                    </div>
                </#if>
        <#else>
            <span class="kc-feedback-text">${message.summary?no_esc}</span>
        </#if>

    </div>
    </#if>

    <#if isMainRegistration==false>
        <#nested "form">
    </#if>

    <#if displayInfo>
    <div id="kc-info" class="${properties.kcInfoAreaClass!}">
        <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                    <#nested "info">
                </div>
            </div>
        </#if>

    </section>
</div>




</body>
</html>
</#macro>
