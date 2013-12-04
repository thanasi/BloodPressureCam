# import cv2
# import sys

cv2.namedWindow("preview")
vc = cv2.VideoCapture(0)
vc.set(cv2.cv.CV_CAP_PROP_GAIN, 0.0)
vc.set(cv2.cv.CV_CAP_PROP_EXPOSURE, 0.0)
# vc.set(cv2.cv.CV_CAP_PROP_CONVERT_RGB, True)

rval, frame = vc.read()

ims = []
SAVEME = False

try:
    while True:

      if frame is not None:   
         cv2.imshow("preview", frame)
         ims.append(frame)
      rval, frame = vc.read()

      # if SAVEME:
      #   ims.append(frame)

      # if cv2.waitKey(1) & 0xFF == ord('s'):
      #   print "saving"
      #   SAVEME = True

      if cv2.waitKey(1) & 0xFF == ord('q'):
        break

except:
    pass
finally:
    sys.stdout.write("\n")
    vc.release()
    cv2.destroyWindow("preview")

# ims2 = np.zeros((len(ims), ims[0].shape[0], ims[0].shape[1], 3), dtype=ims[0].dtype)
ims2 = np.zeros((len(ims), ims[0].shape[0], ims[0].shape[1]), dtype=ims[0].dtype)
for i in range(len(ims)):
    # ims2[i] = cv2.cvtColor(ims[i], cv2.cv.CV_BGR2RGB)
    # ims2[i] = cv2.cvtColor(ims[i], cv2.cv.CV_BGR2GRAY)
    ims2[i] = ims[i][:,:,0].copy()
