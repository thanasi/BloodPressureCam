% This file is part of LightSpeak 
% Copyright © 2013 Nokia Corporation and/or its subsidiary(-ies). All rights reserved.
% Contact: Pranav Mishra <pranav.mishra@nokia.com>
% This software, including documentation, is protected by copyright controlled by Nokia Corporation.
% All rights are reserved. Copying, including reproducing, storing, adapting or translating, any or
% all of this material requires the prior written consent of Nokia Corporation.
% This material also contains confidential information which may not be disclosed to others
% without the prior written consent of Nokia.
%
% For more info about LightSpeak, refer to
% https://research.nokia.com/lightspeak
% 
% Light decode pattern: 
%    start bit - 1, light ID - number of following 0's
%    Ex: 2 = 100100100100100100100100
%        3 = 100010001000100010001000
% This code assumes that each bit spans two consecutive rows of the frame

clear all;
close all;
fclose all;
clc;

nWidth = 1280;
nHeight= 720;
line_scan = 698;    % Column in the frame to be used for decoding

if 1
    % READ A VIDEO FILE, use one of the frames to decode the light ID
    fname = '../media/ThorlabsVideos/5';
    readerobj = VideoReader([fname,'.avi'], 'tag', 'myreader1');
    numFrames = get(readerobj, 'NumberOfFrames');
    frm = 1;
    while (frm < numFrames)
       % Read in all video frames.
       vidFrame = read(readerobj,frm);
       I1 = mean2(vidFrame(:,line_scan,1));
       framelist(frm,:) = [frm I1];
       frm = frm + 1;
    end

    selectedframe = find(max(framelist(:,2))== framelist(:,2));
else
    selectedframe = 1;   
end

maxI = 0;

for frame = selectedframe:selectedframe
    vidFrame = read(readerobj,frame);
    %vidFrame  = imread('coded_image.jpg');
    I = rgb2gray(vidFrame(:,:,:));
    
    if 0
        % View the intensity values in the frame
        figure;
        stem(I(1:nHeight,line_scan),'MarkerSize',2);
        ylabel('intensity');
        xlabel('row num');
        grid
    end

    %Reciever decoding the above
    code = zeros(1024,1);
    prevSymbol = I(1,line_scan);
    prevVal1   = 0;
    run        = 0;
    decodedBitSequence = zeros(512,1);
    transition = 1;
    m = -1;
    j = 1;
    k = 1;

    maxSymbol = 0;
    totalbit  = 0;
    val1      = 0;
    prevprevSymbol = 0;
    noiselevel = 40;
    for i = 2:nHeight-1
        if i > 16
            prevmaxSymbol = double(max(I(i-16:i,line_scan)));
        else
            prevmaxSymbol = double(max(I(1:16,line_scan)));   
        end
        
        if i < nHeight-16
            maxSymbol = double(max(I(i:i+16,line_scan))); 
        else
            maxSymbol = double(max(I(nHeight-16:nHeight,line_scan)));    
        end
        
        if maxSymbol < 40
            maxSymbol = 40;
        end
        
        if maxSymbol < prevmaxSymbol
            maxSymbol = prevmaxSymbol;
        end
       
        currSymbol = I(i,line_scan);
        nextSymbol = I(i+1,line_scan);
        val1= double(currSymbol) - double(prevSymbol);
        val2= double(nextSymbol) - double(currSymbol);
        
        if ( ((val1 > maxSymbol/10 && currSymbol > noiselevel)&& (val1 + val2) > double(maxSymbol/5)) && transition == 1 ) %transition condition
            if run ~= 0
                k   = bitshift(run-1,-1);   % Shift by 1 assuming width of one bit is two rows of the frame
            else
                k = 0;
            end
            for l = 1:k
                decodedBitSequence(j)=0;
                j = j+1;
            end
                
            decodedBitSequence(j) = 1;
            if currSymbol > maxI
                maxI = currSymbol;
            end
                
            run = 1;
            transition   = 0;
            j = j + 1;
            m = 0;
        elseif ( (val1+val2 < -double(maxSymbol/5)) && transition == 0)  
            k   = bitshift(run-1,-1);      % Shift by 1 assuming width of one bit is two rows of the frame
            for l = 1:k
                decodedBitSequence(j)=1;
                j = j+1;
            end
            decodedBitSequence(j) = 0;
            transition   = 1;
            j = j + 1;
            m = 0;  
            run = 1;
        else    
            if m~=-1
                run = run+1;
                if transition == 0
                    if currSymbol > maxI
                        maxI = currSymbol;
                    end
                end
            end

        end
    
        prevprevSymbol = prevSymbol;
        prevSymbol = currSymbol;

    end
end

%Get the decoced light ID
indexCurr = 1;

%Skip to the first zero in the bit sequence
while decodedBitSequence(indexCurr) == 0
    indexCurr = indexCurr + 1;
end
indexCurr = indexCurr + 1;

zeroCount = zeros(1,17); % Size is 17 to restict number of IDs to 16
runningZeroCount = 0;
for i= indexCurr:numel(decodedBitSequence)
    if decodedBitSequence(i) == 0
        runningZeroCount = runningZeroCount + 1;
    else
        zeroCount(1,runningZeroCount+1) = zeroCount(1,runningZeroCount+1) + 1;
        runningZeroCount = 0;
    end
end

%get the lightID
[val, lightID] = max(zeroCount);
lightID = lightID - 1;
disp(['Light ID = ', int2str(lightID)]);




