//
//  TRTCKaraokeViewController.swift
//  TRTCKaraokeDemo
//
//  Created by abyyxwang on 2020/6/8.
//  Copyright © 2020 tencent. All rights reserved.
//
import UIKit
import TUICore

protocol TRTCKaraokeViewModelFactory {
   func makeKaraokeViewModel(roomInfo: RoomInfo, roomType: KaraokeViewType) -> TRTCKaraokeViewModel
}

/// TRTC voice room 聊天室
public class TRTCKaraokeViewController: UIViewController {
    // MARK: - properties:
    let viewModelFactory: TRTCKaraokeViewModelFactory
    let roomInfo: RoomInfo
    let role: KaraokeViewType
    var viewModel: TRTCKaraokeViewModel?
    let toneQuality: KaraokeToneQuality
    let musicDataSource: KaraokeMusicService
    // MARK: - Methods:
    init(viewModelFactory: TRTCKaraokeViewModelFactory, roomInfo: RoomInfo, role: KaraokeViewType, toneQuality: KaraokeToneQuality = .music, musicDataSource: KaraokeMusicService) {
        self.viewModelFactory = viewModelFactory
        self.roomInfo = roomInfo
        self.role = role
        self.toneQuality = toneQuality
        self.musicDataSource = musicDataSource
        KaraokeMusicCacheDelegate.musicDataSource = musicDataSource
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    // MARK: - life cycle
    public override func viewDidLoad() {
        super.viewDidLoad()
        guard let model = viewModel else { return }
        if model.isOwner {
            model.createRoom(toneQuality: toneQuality.rawValue)
        } else {
            model.enterRoom()
        }
#if RTCube_APPSTORE
        let selector = NSSelectorFromString("showAlertUserLiveTips")
        if responds(to: selector) {
            perform(selector)
        }
#endif
        TUILogin.add(self)
    }
    
    public override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.navigationController?.setNavigationBarHidden(true, animated: false)
    }
    
    public override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        viewModel?.refreshView()
    }
    
    public override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        self.navigationController?.setNavigationBarHidden(false, animated: false)
    }
    
    public override func loadView() {
        // Reload view in this function
        let viewModel = viewModelFactory.makeKaraokeViewModel(roomInfo: roomInfo, roomType: role)
        let rootView = TRTCKaraokeRootView.init(viewModel: viewModel)
        rootView.rootViewController = self
        viewModel.viewResponder = rootView
        viewModel.rootVC = self
        viewModel.musicDataSource = musicDataSource
        self.viewModel = viewModel
        view = rootView
    }
    
    deinit {
        TUILogin.remove(self)
        TRTCLog.out("deinit \(type(of: self))")
    }
}

// MARK: - TUILoginListener
extension TRTCKaraokeViewController: TUILoginListener {
    
    public func onConnecting() {
        
    }
    
    public func onConnectSuccess() {
        
    }
    
    public func onConnectFailed(_ code: Int32, err: String!) {
        
    }
    
    public func onKickedOffline() {
        if TRTCKaraokeFloatingWindowManager.shared().windowIsShowing {
            TRTCKaraokeFloatingWindowManager.shared().closeWindowAndExitRoom()
        } else {
            viewModel?.exitRoom {
                
            }
        }
    }
    
    public func onUserSigExpired() {
        
    }
    
}

extension TRTCKaraokeViewController {
    func presentAlert(title: String, message: String, sureAction:@escaping () -> Void) {
        let alertVC = UIAlertController.init(title: title, message: message, preferredStyle: .alert)
        let alertOKAction = UIAlertAction.init(title: .confirmText, style: .default) { (action) in
            alertVC.dismiss(animated: true, completion: nil)
            sureAction()
        }
        let alertCancelAction = UIAlertAction.init(title: .cancelText, style: .cancel) { (action) in
            alertVC.dismiss(animated: true, completion: nil)
        }
        alertVC.addAction(alertCancelAction)
        alertVC.addAction(alertOKAction)
        present(alertVC, animated: true, completion: nil)
    }
}

private extension String {
    static let exitText = karaokeLocalize("Demo.TRTC.Karaoke.exit")
    static let sureToExitText = karaokeLocalize("Demo.TRTC.Karaoke.isvoicingandsuretoexit")
    static let confirmText = karaokeLocalize("Demo.TRTC.LiveRoom.confirm")
    static let cancelText = karaokeLocalize("Demo.TRTC.LiveRoom.cancel")
}


