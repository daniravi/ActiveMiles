-- Face detector demo ported to Android. 
-- Script: main.lua
-- Author: Vinayak Gokhale
-- This script loads the network into a lua global "network"
--The function "getDetections" is called for each frame and detections
--are sent back to C as x,y coordinates and height and width of the box to be drawn.

android=1
require 'torchandroid'
require 'torch'
require 'nn'
require 'dok'
require 'image'


network = torch.load('Ws-1Rnd-5Sec-cs1model.net','apkbinary64'):float()
print('OK1')
mean = torch.load('Ws-1Rnd-5Sec-cs1avg.dat','apkbinary64')
print('OK2')
std = torch.load('Ws-1Rnd-5Sec-cs1std.dat','apkbinary64')
print('OK3')
network:float()
network:evaluate();
torch.setdefaulttensortype('torch.FloatTensor')


function getDetections(network,mean,std,testTensor) 
	nFeat=102
	--print(tostring(std))
	--print(tostring(mean))
	--print('nonNormal'..tostring(testTensor))
	for i=1,nFeat do
	    testTensor[{{i}}]:add(-mean[i])
		if std[i]~=0 then
		   testTensor[{{i}}]:div(std[i])   
	   end
	 
    end
	--print('Normal'..tostring(testTensor))	
    start = os.clock()  
    y=testTensor:reshape(1,nFeat):float()      
    pred = network:forward(y)
    y,class=torch.sort(pred,2,true)
    stop = os.clock()
	classstring=tostring(class)
    confidenceString=tostring(y)   
	return classstring,confidenceString			
end

