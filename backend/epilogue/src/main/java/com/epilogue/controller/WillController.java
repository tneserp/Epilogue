package com.epilogue.controller;

import com.epilogue.domain.will.Will;
import com.epilogue.dto.request.viewer.ViewerRequestDto;
import com.epilogue.dto.request.will.WillAdditionalRequestDto;
import com.epilogue.dto.request.will.WillApplyRequestDto;
import com.epilogue.dto.request.will.WillMemorialRequestDto;
import com.epilogue.dto.request.witness.WitnessRequestDto;
import com.epilogue.service.AwsS3Service;
import com.epilogue.service.ViewerService;
import com.epilogue.service.WillService;
import com.epilogue.service.WitnessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.List;

@Slf4j
@Tag(name = "Will Controller", description = "유언 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/will")
public class WillController {
    private final WillService willService;
    private final WitnessService witnessService;
    private final ViewerService viewerService;
    private final AwsS3Service awsS3Service;

    @Operation(summary = "유언 파일 및 증인 저장 API", description = "유언 파일 및 증인을 저장합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping(value = "/willAndWitness")
    public ResponseEntity<Void> saveWillAndWitness(@Parameter(description = "유언 열람 파일 (multipart/form-data 타입)") @RequestPart MultipartFile multipartFile,
                                                   @Parameter(description = "증인 목록 (application/json 타입)") @RequestPart List<WitnessRequestDto> witnessList, Principal principal) {
        // 임의 유언 생성
        Will will = new Will();
        willService.saveWill(will);

        // 증인 리스트 저장
        witnessService.saveWitness(will, witnessList, principal);

        // 블록체인 트랜잭션 생성 (해시, 녹음 파일 url, 초기 영수증)

        // 블록체인 생성이 성공적으로 됐을 경우


        // 유언 파일 S3 저장 (원본 파일, 초기 영수증)
        awsS3Service.uploadWill(multipartFile, principal);

        // 프론트에 알림 (200 보내기)
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "열람인 저장 API", description = "열람인을 저장합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/viewer")
    public ResponseEntity<Void> saveViewer(@Parameter(description = "열람인 목록") @RequestBody List<ViewerRequestDto> viewerList, Principal principal) {
        // 열람인 리스트 저장
        viewerService.save(viewerList, principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "디지털 추모관 정보 저장 API", description = "디지털 추모관 정보를 저장합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/memorial")
        public ResponseEntity<Void> saveMemorial(@Parameter(description = "묘비 사진 파일 (multipart/form-data 타입)") @RequestPart MultipartFile multipartFile,
                                                 @Parameter(description = "디지털 추모관 정보 요청 DTO (application/json 타입)")  @RequestPart WillMemorialRequestDto willMemorialRequestDto, Principal principal) {
        // 묘비 사진 S3 저장
        awsS3Service.uploadGraveImage(multipartFile, principal);

        // 디지털 추모관 정보 저장
        willService.saveMemorial(willMemorialRequestDto, principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "추가 정보 저장 API", description = "추가 정보를 저장합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @PostMapping("/additional")
    public ResponseEntity<Void> saveAdditionalInformation(@Parameter(description = "추가 정보 요청 DTO") @RequestBody WillAdditionalRequestDto willAdditionalRequestDto, Principal principal) {
        // 추가 정보 저장
        willService.saveAdditionalInformation(willAdditionalRequestDto, principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "나의 유언 조회 API", description = "내가 작성한 유언을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping
    public ResponseEntity<String> viewMyWill(Principal principal) {
        // S3에서 가져온 유언 파일 반환
        return new ResponseEntity<>(willService.viewMyWill(principal), HttpStatus.OK);
    }

    @Operation(summary = "나의 유언 삭제 API", description = "내가 작성한 유언을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @DeleteMapping
    public ResponseEntity<Void> deleteMyWill(Principal principal) throws MalformedURLException, UnsupportedEncodingException {
        willService.deleteMyWill(principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "유언 열람 신청 API", description = "유언 열람을 신청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "true", description = "인증 성공"),
            @ApiResponse(responseCode = "false", description = "인증 실패"),
    })
    @PostMapping("/apply")
    public ResponseEntity<Boolean> applyWill(@Parameter(description = "유언 열람 인증 요청 DTO") @RequestBody WillApplyRequestDto willApplyRequestDto) {
        return new ResponseEntity<>(willService.applyWill(willApplyRequestDto), HttpStatus.OK);
    }
}
