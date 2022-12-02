


function loginCheck() {
    $.ajax({
        type : "POST",
        url: '/login',
        data: {
            username: $('#username').val(),
            password: $('#password').val()
        },
        success: function(data) {
            alert('안녕하세요 ' + $('#username').val() + '님') ;
            location.href=data;
        },
        error: function(status) {
           $(status.responseJSON).each(function(){
               if(this.field===('username')){
                   $('.usernameCheck').text(this.message);
                   $('.usernameCheck').css('display', 'block');
               }
                if(this.field===('password')){
                    $('.passwordCheck').text(this.message);
                    $('.passwordCheck').css('display', 'block');
                }
           }
              )
        }
    }) ;
}
$("#userId").blur(function(){
    var userId = $("#userId").val();
    if(userId == "" || userId.length > 25 || userId.length<6){
        $(".idCheck").text("이름은 6자 이상 25자 이하로 설정해주세요.");
        $(".idCheck").css("color", "red");
        $("#idDoubleChk").val("false");
    }else{
        $.ajax({
            url : '/user/idCheck?userId='+userId,
            type : 'post',
            cache : false,
            success : function() {
                $(".idCheck").text("사용중인 아이디입니다.");
                $(".idCheck").css("color", "red");
                $("#idDoubleChk").val("false");
            } ,
            error: function() {
                $(".idCheck").text("사용가능한 아이디입니다.");
                $(".idCheck").css("color", "green");
                $("#idDoubleChk").val("true");
            }
        });
    }
});
$("#password1").blur(function(){
    var password1 = $("#password1").val();
    var reg = /^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$/;
    if(reg.test(password1)==false){
        $(".passwordCheck").text("비밀번호를 올바르게 입력해주세요.");
        $(".passwordCheck").css("color", "red");
    }else{
        $(".passwordCheck").text("사용가능한 비밀번호입니다.");
        $(".passwordCheck").css("color", "green");
    }
});
$("#password2").blur(function(){
    var password2 = $("#password2").val();
    var password1 = $("#password1").val();
    if(password2 != password1){
        $(".passwordInvalidCheck").text("비밀번호가 일치하지 않습니다");
        $(".passwordInvalidCheck").css("color", "red");
    }else{
        $(".passwordInvalidCheck").text("비밀번호가 일치합니다.");
        $(".passwordInvalidCheck").css("color", "green");
    }
});
$("#nickname").blur(function(){
    var nickname = $("#nickname").val();
    if(nickname == "" || nickname.length > 10 || nickname.length<2){
        $(".nicknameCheck").text("닉네임은 2자 이상 10자 이하로 설정해주세요.");
        $(".nicknameCheck").css("color", "red");
        $("#nicknameDoubleChk").val("false");
    }else{
        $.ajax({
            url : '/user/nicknameCheck?nickname='+nickname,
            type : 'post',
            cache : false,
            success : function() {
                $(".nicknameCheck").text("사용중인 닉네임입니다.");
                $(".nicknameCheck").css("color", "red");
                $("#nicknameDoubleChk").val("false");
            } ,
            error: function() {
                $(".nicknameCheck").text("사용가능한 닉네임입니다.");
                $(".nicknameCheck").css("color", "green");
                $("#nicknameDoubleChk").val("true");
            }
        });
    }
});
$("#email").blur(function(){
    var email = $("#email").val();
    var regExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$/i;
    if(email == "" || email.match(regExp) == null){
        $(".emailCheck").text("올바른 이메일 주소를 입력해주세요.");
        $(".emailCheck").css("color", "red");
        $("#emailDoubleChk").val("false");
    }else{
        $.ajax({
            url : '/user/emailCheck?email='+email,
            type : 'post',
            cache : false,
            success : function() {
                $(".emailCheck").text("사용중인 이메일입니다.");
                $(".emailCheck").css("color", "red");
                $("#emailDoubleChk").val("false");
            } ,
            error: function() {
                $(".emailCheck").text("사용가능한 이메일입니다.");
                $(".emailCheck").css("color", "green");
                $("#emailDoubleChk").val("true");
            }
        });
    }
});


function signupCheck() {
    var filearray
    var formData = new FormData();
    formData.append("imgFile", $("#imgFile")[0].files[0]);
    var data = {
        info: {
            userId: $("#userId").val(),
            password1: $("#password1").val(),
            password2: $("#password2").val(),
            nickname: $("#nickname").val(),
            email: $("#email").val(),
            memo: $("#memo").val()
        }
    }
    formData.append(
        "signupDto",
        new Blob([JSON.stringify(data.info)], { type: "application/json" })
    );

    console.log(formData);
    //위에서 만든 오브젝트를 json 타입으로 바꾼다.
    $.ajax({
        type : 'POST',
        url : '/signup',
        data : formData,
        processData : false,
        contentType : false,
        success: function ( ){
            alert("회원가입 성공");
        },
        error: function (status) {
            console.log(status);
            $(status.responseJSON).each(function(){
                if(this.field===('userId')){
                    $(".idCheck").text(this.message);
                    $(".idCheck").css("color", "red");
                }
                if(this.field===('password1')){
                    $(".passwordCheck").text(this.message);
                    $(".passwordCheck").css("color", "red");
                }
                if(this.field===('password2')){
                    $(".passwordInvalidCheck").text(this.message);
                    $(".passwordInvalidCheck").css("color", "red");
                }
                if(this.field===('nickname')){
                    $(".nicknameCheck").text(this.message);
                    $(".nicknameCheck").css("color", "red");
                }
                if(this.field===('email')){
                    $(".emailCheck").text(this.message);
                    $(".emailCheck").css("color", "red");
                }
                if(this.field===('memo')){
                    $(".memoCheck").text(this.message);
                    $(".memoCheck").css("color", "red");
                }
            });

        }
    });
}

const markingErrorField = function (response) {
    const errorField = response.error()
};