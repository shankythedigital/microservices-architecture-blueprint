package com.example.asset.controller;

import com.example.asset.dto.AssetDto;
import com.example.asset.entity.AssetMaster;
import com.example.asset.service.AssetService;
import com.example.asset.util.ResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService service;

    public AssetController(AssetService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<ResponseWrapper<AssetMaster>> create(@RequestBody AssetDto dto) {
        try {
            AssetMaster created = service.createAsset(dto);
            return ResponseEntity.ok(new ResponseWrapper<>(true, "Asset created successfully", created));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(new ResponseWrapper<>(false, "Failed to create asset: " + ex.getMessage(), null));
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseWrapper<AssetMaster>> get(@PathVariable Long id) {
        return service.getAsset(id).map(a -> ResponseEntity.ok(new ResponseWrapper<>(true, "Asset found", a)))
                .orElseGet(() -> ResponseEntity.status(404).body(new ResponseWrapper<>(false, "Asset not found", null)));
    }
}
