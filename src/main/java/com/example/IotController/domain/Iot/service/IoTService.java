package com.example.IotController.domain.Iot.service;

import com.example.IotController.domain.Device.model.Plugs;
import com.example.IotController.domain.Device.service.PlugService;
import com.example.IotController.domain.Energy.dto.EnergyResponse;
import com.example.IotController.domain.Energy.model.Energy;
import com.example.IotController.domain.Energy.service.EnergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IoTService {

    private final PlugService plugService;
    private final EnergyService energyService;

    public void autoOff(Long id) {
        // 아래꺼를 기준으로 충족되면 호출
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void getIoTState() {

        List<Plugs> plugs = plugService.findByMode();

        List<Long> plugsId = plugs.stream().map(p -> p.getId()).collect(Collectors.toList());

        for (Long id : plugsId) {

            RestTemplate restTemplate = new RestTemplate();
            String serverlessUrl = "https://e9pn9q3v9l.execute-api.ap-northeast-2.amazonaws.com/rescue_first_deploy_api/getPowerUsage";

            HttpEntity<Long> entity = new HttpEntity<>(id);

            // Flask 서버에 POST 요청 보내기
            ResponseEntity<EnergyResponse> response = restTemplate.exchange(serverlessUrl, HttpMethod.GET, entity, EnergyResponse.class);

            EnergyResponse energyResponse = response.getBody();

            Long responseId = energyResponse.getId();
            String responseStatus = energyResponse.getStatus();
            Double powerUsage = energyResponse.getPowerUsage();

            if (responseStatus == "off") {
                continue;
            }

            if (powerUsage != 0) {
                energyService.deleteFalse(responseId);
                continue;
            }

            Boolean existsPlug = energyService.existsByPlugId(responseId);
            Energy energy = energyService.findById(responseId);
            Plugs plug = plugService.findById(responseId);
            if (existsPlug) {
                energy.plusStack(energy);
            }

            if (!existsPlug){
                energyService.save(plug, powerUsage);
            }

            if (energy.getStack() / 12 >= plug.getTime()) {
                autoOff(plug.getId());
            }
        }

        List<Plugs> falsePlugs = plugService.findByModeIsFalse();

        List<Long> falseIds = falsePlugs.stream().map(p -> p.getId()).collect(Collectors.toList());

        for (Long id : falseIds) {
            energyService.deleteFalse(id);
        }
    }
}
