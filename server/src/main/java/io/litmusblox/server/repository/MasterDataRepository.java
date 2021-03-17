package io.litmusblox.server.repository;

import io.litmusblox.server.model.MasterData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author : Shital Raval
 * Date : 4/7/19
 * Time : 1:34 PM
 * Class Name : MasterDataRepository
 * Project Name : server
 */
@Repository
public interface MasterDataRepository extends JpaRepository<MasterData, Long> {

    @Transactional(readOnly = true)
    List<MasterData> findByTypeOrderByValueToUSe(String type);

    @Transactional(readOnly = true)
    List<MasterData> findByTypeAndValue(String type,String value);
}
