package team.cheese.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.cheese.entity.ImgFactory;
import team.cheese.dao.ImgDao;
import team.cheese.domain.ImgDto;
import team.cheese.exception.DataFailException;
import team.cheese.exception.ImgNullException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ImgService {
    @Autowired
    ImgDao imgDao;
    ImgFactory ifc = new ImgFactory();

    public ImgService() {}

    //아래 테스트코드에서 쓰기위한객체
    public ImgService(ImgDao imgdao2){
        this.imgDao = imgdao2;
    }

    public List<ImgDto> uploadimg(MultipartFile[] uploadFiles){
        if(!ifc.CheckImg(uploadFiles)){
            return null;
        }

        List<ImgDto> list = ifc.makeImg(uploadFiles, "r", 78, 78);

        return list;
    }

    public File display(String fileName){
        String folderPath = ifc.getFolderPath()+ File.separator;

        File file = new File(folderPath+fileName);
        return file;
    }

    @Transactional
    public String reg_img2(ArrayList<ImgDto> imgList, int gno) {
        if (imgList.isEmpty()) {
            throw new ImgNullException("이미지 리스트가 비어 있습니다.");
        }

        boolean cnt = true;
        String spath = "";

        for(ImgDto idto : imgList){
//            idto.setW_size(-1);
            if (idto.getW_size() < 0 || idto.getH_size() < 0) {
                throw new ImgNullException("이미지 사이즈가 올바르지 않습니다.");
            }

            String folderpath = ifc.getFolderPath() + File.separator + ifc.getDatePath();

            File file = new File(folderpath, idto.getO_name()+idto.getE_name());
            if(cnt) {
                spath = idto.getImg_full_rt();
                // 썸네일은 한개만
                ImgDto simg = ifc.makeImg(file, "s", gno, 292, 292);
                imgDao.insert(simg);
                imgDao.insert(imggroup(gno, simg.getNo()));
                cnt = false;
            }

            //미리보기
            imgDao.insert(idto);
            imgDao.insert(imggroup(gno, idto.getNo()));

            //본문
            ImgDto wimg = ifc.makeImg(file, "w", gno, 856, 856);
            imgDao.insert(wimg);
            imgDao.insert(imggroup(gno, wimg.getNo()));
        }

        return spath;
    }

    @Transactional
    public String modify_img(ArrayList<ImgDto> imgList, int gno, int salegno) {
        if (imgList.isEmpty()) {
            throw new IllegalArgumentException("이미지 리스트가 비어 있습니다.");
        }

        boolean cnt = true;
        String spath = "";

        //기존 이미지 상태 전부 비활성화
        HashMap map = new HashMap<>();
        map.put("state", "N");
        map.put("no", salegno);
        update(map);

        for(ImgDto idto : imgList){
            System.out.println("idto : "+idto);
//            if (idto.getW_size() < 0 || idto.getH_size() < 0) {
//                throw new IllegalArgumentException("이미지 사이즈가 올바르지 않습니다.");
//            }

            String folderpath = ifc.getFolderPath() + File.separator + idto.getFile_rt();

            File file = new File(folderpath, idto.getO_name()+idto.getE_name());
            if(cnt) {
                spath = idto.getImg_full_rt();
                // 썸네일은 한개만
                ImgDto simg = ifc.makeImg(file, "s", gno, 292, 292);
                imgDao.insert(simg);
                imgDao.insert(imggroup(gno, simg.getNo()));
                cnt = false;
            }

            //미리보기
            if(idto.getU_name() == null){
                ImgDto rimg = ifc.makeImg(file, "r", gno, 78, 78);
                imgDao.insert(rimg);
                imgDao.insert(imggroup(gno, rimg.getNo()));
            } else {
                imgDao.insert(idto);
                imgDao.insert(imggroup(gno, idto.getNo()));
            }

            //본문
            ImgDto wimg = ifc.makeImg(file, "w", gno, 856, 856);
            imgDao.insert(wimg);
            imgDao.insert(imggroup(gno, wimg.getNo()));
        }

        return spath;
    }

    public ArrayList<ImgDto> readall() {
        return imgDao.select_all_img();
    }

    public ArrayList<ImgDto> read(HashMap map){
        return imgDao.select_img(map);
    }

    //rollbackFor = 지정된 예외
    @Transactional
    public boolean reg_img(int gno, ImgDto idto) throws Exception {
        if(idto == null){
            throw new ImgNullException("이미지가 비어 있습니다.");
        }
//        내부적으로 발생한 예외는 try-catch문으로 날려주는수밖에 없다
//        try {
            int img = imgDao.insert(idto);
            int img2 = imgDao.insert(imggroup(gno, idto.getNo()));
            img = 0;
            if (img <= 0 || img2 <= 0) {
                throw new DataFailException("이미지 등록 오류");
            }
//        } catch (DataIntegrityViolationException e){
//            throw new DataFailException("이미지가 비어 있습니다.");
//        }
        return true;
    }

    public int update(HashMap map){
        return imgDao.update(map);
    }

    private HashMap imggroup(int gno, int imgno){
        HashMap map = new HashMap<>();
        map.put("group_no", gno);
        map.put("img_no", imgno);
        return map;
    }

    public void delete(String tb_name){
        imgDao.delete(tb_name);
    }

    public int count(String tb_name){
        return imgDao.count(tb_name);
    }

    public int getGno(){
        return imgDao.select_group_max();
    }
}
