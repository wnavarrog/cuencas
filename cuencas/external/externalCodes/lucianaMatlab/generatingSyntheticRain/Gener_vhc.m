function Gener_vhc(filename,RES,ncol,nrows,MINLON,MINLAT,Z)

        fid2=fopen(filename,'w');

            for il=nrows:-1:1
               for ic=1:ncol
               fwrite(fid2,Z(il,ic),'float','ieee-be');
               end    
            end

        fclose(fid2);
           