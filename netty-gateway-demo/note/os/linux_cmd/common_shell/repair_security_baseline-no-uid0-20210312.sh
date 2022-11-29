#!/bin/bash

# -*-Shell-script-*-
#
# functions     This script is used to repair security baseline.
#

PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:/root/bin

# 显示脚本版本更新时间
echo "本次使用的版本更新时间是2021-3-2，本脚本不检查和删除非认定的特权账号。"
echo 


#  使用root权限运行脚本
[ $UID -ne 0 ] && {
	id
	echo "当前运行的用户$USER的uid不为0，请以root权限运行。"
	exit 1
}


# 仅支持红帽类操作系统
[ -f /etc/redhat-release ] || {
	echo "仅支持redhet\centos\ole 操作系统。"
	exit 1
}


# 检查OS版本
os_version=`cat /etc/redhat-release |tr -cd '0-9\n' |cut -c 1`


# 不再支持RHEL\CentOS\OEL 4及以下的系统
if [ $os_version -le 4  ];then
	echo "此基线修复脚本仅适用于Redhat Enterprise Linux 5及以上发行版本，Oracle Linux 5及以上发行版本，CentOS 5及以上发行版本。"
	exit 1
fi

# 设置语言编码
LANG="en_US.utf-8"

# 确定主机日期和基线修复目录
date_time=$(date +%F) 
[ -d /root/repair_baseline_dir ] || mkdir -p /root/repair_baseline_dir


# 回滚
if [[ $1 == "Y" || $1 == "y" ]]; then

	if [ -f /root/repair_baseline_dir/rollbackdone ];then
		rollback_time=$(cat /root/repair_baseline_dir/rollbackdone)
		#echo "Rollback on ${rollback_time}, no need to repeat."
		echo "${rollback_time}已回滚过，无需重复操作。"
		exit 0
		
	else
		if [ -f /root/repair_baseline_dir/repairdone ]; then
			repair_date_time=$(cat /root/repair_baseline_dir/repairdone)
		else
			#echo "No recent repairs have been performed, no rollback required."
			echo "最近没有修复过，无需回滚操作。"
			exit 0
		fi
		
		if [ -f /etc/ssh/sshd_config-${repair_date_time} ]; then
			cp /etc/ssh/sshd_config-${repair_date_time} /etc/ssh/sshd_config
			if [ -x /bin/systemctl ];then
				systemctl reload sshd &>/dev/null
			else
				/etc/init.d/sshd reload &>/dev/null
			fi

		fi
		
		
		if [ -f /etc/profile-${repair_date_time} ]; then
			cp /etc/profile-${repair_date_time} /etc/profile
		fi
			
		
		if [ -f /etc/pam.d/system-auth-${repair_date_time} ]; then
			cp /etc/pam.d/system-auth-${repair_date_time} /etc/pam.d/system-auth
		fi
		
		if [ -f /etc/shadow-${repair_date_time} ]; then
			cp /etc/shadow-${repair_date_time} /etc/shadow
		fi
		if [ -f /etc/group-${repair_date_time} ]; then
			cp /etc/group-${repair_date_time} /etc/group
		fi
		if [ -f /etc/passwd-${repair_date_time} ]; then
			cp /etc/passwd-${repair_date_time} /etc/passwd
		fi
		
		
		echo "${date_time}" > /root/repair_baseline_dir/rollbackdone
		mv /root/repair_baseline_dir/repairdone /root/repair_baseline_dir/repairdone-${repair_date_time}
		#echo "Rollback successfully."
		echo "回滚成功。"
		exit 0

	fi
fi


# 检查是否曾经修复过
if [ -f /root/repair_baseline_dir/repairdone ]; then

		if [ -f /root/repair_baseline_dir/rollbackdone ]; then
			rollback_time=$(cat /root/repair_baseline_dir/rollbackdone)
			mv /root/repair_baseline_dir/rollbackdone /root/repair_baseline_dir/rollbackdone-$rollback_time
		fi
		
		repair_date_time=$(cat /root/repair_baseline_dir/repairdone)
		echo "${repair_date_time}已经修复过，现强制重新执行修复："
		
fi


# 备份文件
\cp /etc/shadow /etc/shadow-${date_time} || {
	echo "备份文件失败，请检查系统磁盘或者文件夹权限"
	exit 1
}
[ -f /etc/shadow-bak-${date_time} ] || cp /etc/shadow /etc/shadow-bak-${date_time}

\cp  /etc/group /etc/group-${date_time}
[ -f /etc/group-bak-${date_time} ] || cp /etc/group /etc/group-bak-${date_time}

\cp /etc/passwd /etc/passwd-${date_time}
[ -f //etc/passwd-bak-${date_time} ] || cp /etc/passwd /etc/passwd-bak-${date_time}

\cp /etc/ssh/sshd_config /etc/ssh/sshd_config-${date_time}
[ -f /etc/ssh/sshd_config-bak-${date_time} ] || cp /etc/ssh/sshd_config /etc/ssh/sshd_config-bak-${date_time}

\cp /etc/profile /etc/profile-${date_time}
[ -f /etc/profile-bak-${date_time} ] || cp /etc/profile /etc/profile-bak-${date_time}

\cp /etc/pam.d/system-auth /etc/pam.d/system-auth-${date_time}
[ -f /etc/pam.d/system-auth-bak-${date_time} ] || cp /etc/pam.d/system-auth /etc/pam.d/system-auth-bak-${date_time}

\cp /etc/login.defs /etc/login.defs-${date_time}
[ -f /etc/login.defs-bak-${date_time} ] || cp /etc/login.defs /etc/login.defs-bak-${date_time}

# 检查UID为0，除了root，ada，pacap以外的账号，如有发现直接禁用
uid0() {

	uid0_num=$(awk -F:  '($3 == 0 && $1 != "root" && $1 != "pacapadm" && $1 != "ada" && $1 != "paroot")' /etc/passwd | wc -l)

	if [ $uid0_num -ne 0 ];then
		
		awk -F:  '{if($3 == 0 && $1 != "root" && $1 != "pacapadm" && $1 != "ada" && $1 != "paroot") print $1}' /etc/passwd > /root/uid0.log

		for u in $(awk -F:  '{if($3 == 0 && $1 != "root" && $1 != "pacapadm" && $1 != "ada" && $1 != "paroot") print $1}' /etc/passwd); do
			sed -i /^$u:x:0:/d /etc/passwd
			sed -i /^#$u:x:0:/d /etc/passwd
			sed -i /^$u:x:/d /etc/group
			gpasswd -d $u root &>/dev/null
			gpasswd -d $u sys &>/dev/null
			sed -i /^$u:/d /etc/shadow && echo "已删除非认定的特权账号$u。" || echo "请检查系统磁盘空间，或者/etc/shadow文件权限"
		done

	else

		echo "检查没有非认定的特权账号。"
		
	fi
	
	sed -i /^#cloudadmin:x:0:/d /etc/passwd &>/dev/nul
	sed -i /^cloudadmin:x:/d /etc/group &>/dev/nul
	gpasswd -d cloudadmin root &>/dev/null
	gpasswd -d cloudadmin sys &>/dev/null
	sed -i /^cloudadmin:/d /etc/shadow &>/dev/null || echo "请检查系统磁盘空间，或者/etc/shadow文件权限"
	
	sed -i /^#paxf:x:0:/d /etc/passwd
	sed -i /^paxf:x:/d /etc/group
	gpasswd -d paxf root &>/dev/null
	gpasswd -d paxf sys &>/dev/null
	sed -i /^paxf:/d /etc/shadow &>/dev/null
}


# 检查是否有密码为空的，为空口令设置密码
empty_pass() {
	empty_pass_num=$(awk -F: '($2==""){print$1}' /etc/shadow | wc -l)
	if [ $empty_pass_num -ne 0 ];then
		i=0
		echo "发现空密码用户$empty_pass_num个，现在正在给空密码用户设置随机密码："
		
		for u in $(awk -F: '($2==""){print$1}' /etc/shadow); do
			pass=$(openssl rand -base64 10)
			echo "$pass" | passwd --stdin $u &>/dev/null && echo "$i、空密码用户$u已经成功设置了随机密码，如果需要继续使用该用户，请重置密码后再使用。" || echo "$i、空密码用户$u设置随机密码失败，请检查系统磁盘空间，或者/etc/shadow文件权限。"
			i=`expr $i + 1`
		done

	fi
}


# 检查ssh版本是否为1和X11Forwarding转发是否开启
ssh_protocol_X11Forwarding() {
	protocol_check=$(grep -E "^[' ']{0,}Protocol[' ']+1[' ']{0,}$"  /etc/ssh/sshd_config | wc -l)
	if [ $protocol_check -ne 0 ]; then
		
		sed -i /Protocol/s/1/2/ /etc/ssh/sshd_config
		
		if [ -x /bin/systemctl ];then
			systemctl reload sshd.service  > /dev/null	
		else
			/etc/init.d/sshd reload  > /dev/null
		fi
		
	fi
	
	X11Forwarding_check=$(grep -E "^[' ']{0,}X11Forwarding[' ']+no[' ']{0,}$"  /etc/ssh/sshd_config | wc -l)
	sed -i '/X11Forwarding/s/yes/no/' /etc/ssh/sshd_config
	if [ $X11Forwarding_check -eq 0 ]; then

		#sed -i /X11Forwarding/s/yes/no/ /etc/ssh/sshd_config
		grep ^"X11Forwarding no" /etc/ssh/sshd_config &>/dev/null || {
			sed -i '/X11Forwarding/d' /etc/ssh/sshd_config && echo "X11Forwarding no" >> /etc/ssh/sshd_config
		}
		
		if [ -x /bin/systemctl ];then
			systemctl reload sshd.service  > /dev/null	
		else
			/etc/init.d/sshd reload  > /dev/null
		fi
		
	fi
}


# 检查PATH路径是否存在.目录，检查TMOUT值是否小于1800，是否设置export TMOUT和readonly TMOUT
profile_check() {

	dot_num=$(echo $PATH | grep -E ':\.[/]{0,1}' --color | wc -l)
	if [ $dot_num -ne 0 ]; then

		PATH=$(echo $PATH | tr "." ":")
		echo "PATH=$PATH" >> /etc/profile

	fi
	
	umask_check=$(grep ^umask /etc/profile | tail -1 | awk '{if($2 < 022 ) print $2}' | wc -l)
	umask_value=$(grep ^umask /etc/profile | tail -1 | awk '{print $2}')

	sed -i '/umask/s/ 0222$/ 0022/' /etc/profile
	if [ $umask_check -ne 0 ]; then
		sed -i '/^umask=.*/d' /etc/profile
		sed -i /^#umask/d  /etc/profile /etc/bashrc
		grep ^umask /etc/profile &>/dev/null && sed -i /^umask/s/[0-9].*/022/ /etc/profile || echo "umask 022" >> /etc/profile
		sed -i '/umask/s/ 002$/ 022/' /etc/profile
		[ -f /etc/bashrc ] && sed -i /umask/s/[0-9].*/022/ /etc/bashrc

	else
		sed -i '/umask/s/ 002$/ 022/' /etc/profile
		
	fi

	TMOUT_check=$(grep "^TMOUT=" /etc/profile | wc -l)
	readonly_check=$(grep "^readonly TMOUT" /etc/profile | wc -l)
	export_check=$(grep "^export TMOUT" /etc/profile | wc -l)
	
	if [[ $TMOUT_check -eq 0 ]]; then
		
		sed -i /TMOUT/s/^/#/g /etc/profile
		echo "TMOUT=1800" >> /etc/profile
		echo "readonly TMOUT" >> /etc/profile
		echo "export TMOUT" >> /etc/profile
		
	else
		tmout_old=$(awk -F[" "=]+  '/^TMOUT=/ {print $2}' /etc/profile |tail -1)
		
		if [ -z "$tmout_old" ];	then
			
			sed -i s/TMOUT=.*/TMOUT=1800/ /etc/profile
			
			if [[ $export_check -eq 0 ]]; then
				echo -e "readonly TMOUT\nexport TMOUT" >> /etc/profile
					
			else
				sed -i '/export TMOUT/ireadonly TMOUT' /etc/profile
				
			fi
			
			if [[ $export_check -eq 0 ]]; then
				echo "export TMOUT" >> /etc/profile
			fi
		
		elif [[ $tmout_old -gt 1800 ]]; then
		
			sed -i s/TMOUT=.*/TMOUT=1800/ /etc/profile
			
			if [[ $export_check -eq 0 ]]; then
				echo -e "readonly TMOUT\nexport TMOUT" >> /etc/profile
					
			else
				sed -i '/export TMOUT/ireadonly TMOUT' /etc/profile
				
			fi
			
			if [[ $export_check -eq 0 ]]; then
				echo "export TMOUT" >> /etc/profile
			fi
			
		elif [[ $readonly_check -eq 0 ]]; then

			if [[ $export_check -eq 0 ]]; then
				echo -e "readonly TMOUT\nexport TMOUT" >> /etc/profile
				
			else
				sed -i '/export TMOUT/ireadonly TMOUT' /etc/profile
				
			fi
					
		elif [[ $export_check -eq 0 ]]; then
			echo "export TMOUT" >> /etc/profile
					
		fi
	fi
	
}

# 密码口令要求最小长度不小于8；复杂度等于3，并强制对root生效；修改密码必须不能使用最近10次的密码

pass_check() {

# 检查pam文件是否改动较大，如果较大则直接使用模板覆盖
if [[ $(grep "^password.*pam_pwquality.so" /etc/pam.d/system-auth | wc -l) -eq 0 && $(grep "^password.*pam_cracklib.so" /etc/pam.d/system-auth | wc -l) -eq 0 ]]; then
		if [[ $os_version -ge 7 ]];then
cat > /etc/pam.d/system-auth << EOF
#%PAM-1.0
# This file is auto-generated.
# User changes will be destroyed the next time authconfig is run.
auth        required      pam_env.so
auth        required      pam_faildelay.so delay=2000000
auth        sufficient    pam_unix.so nullok try_first_pass
auth        requisite     pam_succeed_if.so uid >= 500 quiet_success
auth        required      pam_deny.so

account     required      pam_unix.so
account     sufficient    pam_localuser.so
account     sufficient    pam_succeed_if.so uid < 1000 quiet
account     required      pam_permit.so

password    requisite     pam_pwquality.so try_first_pass local_users_only retry=3 authtok_type= enforce_for_root  minlen=8 minclass=3
password    sufficient    pam_unix.so sha512 shadow nullok try_first_pass use_authtok remember=10
password    required      pam_deny.so

session     optional      pam_keyinit.so revoke
session     required      pam_limits.so
-session     optional      pam_systemd.so
session     optional      pam_oddjob_mkhomedir.so umask=0077
session     [success=1 default=ignore] pam_succeed_if.so service in crond quiet use_uid
session     required      pam_unix.so			
EOF

else

cat > /etc/pam.d/system-auth << EOF
#%PAM-1.0
# This file is auto-generated.
# User changes will be destroyed the next time authconfig is run.
auth        required      pam_env.so
auth        sufficient    pam_unix.so try_first_pass nullok
auth        required      pam_deny.so

account     required      pam_unix.so

password    requisite     pam_cracklib.so try_first_pass retry=3 type= minclass=3 minlen=8 enforce_for_root
password    sufficient    pam_unix.so try_first_pass use_authtok nullok sha512 shadow remember=10
password    required      pam_deny.so 

session     optional      pam_keyinit.so revoke
session     required      pam_limits.so
session     [success=1 default=ignore] pam_succeed_if.so service in crond quiet use_uid
session     required      pam_unix.so
EOF
fi
fi

	# 检查密码历史记录
	remember_check1=$(grep "^password.*sufficient.*pam_unix.so.*remember=.*" /etc/pam.d/system-auth | wc -l)
	remember_check2=$(grep "^password.*sufficient.*pam_unix.so.*remember=.*" /etc/pam.d/system-auth | grep -Po "remember=.*" | awk -F[" "=]+ '{if($2>=10) print $2}' | wc -l)
	remember_value=$(grep "^password.*sufficient.*pam_unix.so.*remember=.*" /etc/pam.d/system-auth | grep -Po "remember=.*" | awk -F[" "=]+ '{print $2}')

	if [[ $remember_check1 -eq 0 || $remember_check2 -eq 0 ]]; then
	
		if [ $remember_check1 -eq 0 ]; then
			sed -i '/^password.*sufficient.*pam_unix.so/s/$/ remember=10/' /etc/pam.d/system-auth

		else		
			sed -i '/^password.*sufficient.*pam_unix.so/s/remember=.*$/remember=10/' /etc/pam.d/system-auth

		fi
	fi
	
	
	# 检查密码最小长度
	if [[ $os_version -ge 7 ]];then
		minlen_check1=$(grep "^password.*pam_pwquality.so.*minlen=.*" /etc/pam.d/system-auth | wc -l)
		minlen_check2=$(grep "^password.*pam_pwquality.so.*minlen=.*" /etc/pam.d/system-auth | grep -Po "minlen=.*" | awk -F[" "=]+ '{if($2>=8) print $2}' | wc -l)
		minlen_value=$(grep "^password.*pam_pwquality.so.*minlen=.*" /etc/pam.d/system-auth | grep -Po "minlen=.*" | awk -F[" "=]+ '{print $2}')
		
		if [[ $minlen_check1 -eq 0 || $minlen_check2 -eq 0 ]]; then
		
			if [ $minlen_check1 -eq 0 ]; then
				sed -i '/^password.*pam_pwquality.so/s/$/ minlen=8/' /etc/pam.d/system-auth
				
			else				
				sed -i '/^password.*pam_pwquality.so/s/minlen=.*$/minlen=8/' /etc/pam.d/system-auth

			fi
		fi
		
	fi
	
	minlen_check1=$(grep "^password.*pam_cracklib.so.*minlen=.*" /etc/pam.d/system-auth | wc -l)
	minlen_check2=$(grep "^password.*pam_cracklib.so.*minlen=.*" /etc/pam.d/system-auth | grep -Po "minlen=.*" | awk -F[" "=]+ '{if($2>=8) print $2}' | wc -l)
	minlen_value=$(grep "^password.*pam_cracklib.so.*minlen=.*" /etc/pam.d/system-auth | grep -Po "minlen=.*" | awk -F[" "=]+ '{print $2}')
	
	if [[ $minlen_check1 -eq 0 || $minlen_check2 -eq 0 ]]; then

		if [ $minlen_check1 -eq 0 ]; then
			sed -i '/^password.*pam_cracklib.so/s/$/ minlen=8/' /etc/pam.d/system-auth

		else				
			sed -i '/^password.*pam_cracklib.so/s/minlen=.*$/minlen=8/' /etc/pam.d/system-auth

		fi
	fi

	
	
	# 检查密码复杂度
	if [ $os_version -ge 7 ];then
		minclass_check1=$(grep "^password.*pam_pwquality.so.*minclass=.*" /etc/pam.d/system-auth | wc -l)
		minclass_check2=$(grep "^password.*pam_pwquality.so.*minclass=.*" /etc/pam.d/system-auth | grep -Po "minclass=.*" | awk -F[" "=]+ '{if($2>=3) print $2}' | wc -l)
		minclass_value=$(grep "^password.*pam_pwquality.so.*minclass=.*" /etc/pam.d/system-auth | grep -Po "minclass=.*" | awk -F[" "=]+ '{print $2}')
		
		if [[ $minclass_check1 -eq 0 || $minclass_check2 -eq 0 ]]; then
		
			if [ $minclass_check1 -eq 0 ]; then
				sed -i '/^password.*pam_pwquality.so/s/$/ minclass=3/' /etc/pam.d/system-auth
				
			else	
				sed -i '/^password.*pam_pwquality.so/s/minclass=.*$/minclass=3/' /etc/pam.d/system-auth

			fi
		fi
		
		enforce_check=$(grep "^password.*pam_pwquality.so.*enforce_for_root" /etc/pam.d/system-auth | wc -l)
		if [[ $enforce_check -eq 0 ]]; then
			sed -i '/^password.*pam_pwquality.so/s/$/ enforce_for_root/' /etc/pam.d/system-auth
		fi
		
	fi
	
	minclass_check1=$(grep "^password.*pam_cracklib.so.*minclass=.*" /etc/pam.d/system-auth | wc -l)
	minclass_check2=$(grep "^password.*pam_cracklib.so.*minclass=.*" /etc/pam.d/system-auth | grep -Po "minclass=.*" | awk -F[" "=]+ '{if($2>=3) print $2}' | wc -l)
	minclass_value=$(grep "^password.*pam_cracklib.so.*minclass=.*" /etc/pam.d/system-auth | grep -Po "minclass=.*" | awk -F[" "=]+ '{print $2}')
	
	if [[ $minclass_check1 -eq 0 || $minclass_check2 -eq 0 ]]; then

		if [ $minclass_check1 -eq 0 ]; then
			sed -i '/^password.*pam_cracklib.so/s/$/ minclass=3/' /etc/pam.d/system-auth
			
		else
			sed -i '/^password.*pam_cracklib.so/s/minclass=.*$/minclass=3/' /etc/pam.d/system-auth
			
		fi
	fi
	
	enforce_check=$(grep "^password.*pam_cracklib.so.*enforce_for_root" /etc/pam.d/system-auth | wc -l)
	if [[ $enforce_check -eq 0 ]]; then
		sed -i '/^password.*pam_cracklib.so/s/$/ enforce_for_root/' /etc/pam.d/system-auth
	fi

}

# 检查密码最短修改时间为7天，最长时间为90天，密码过期警告为15天
pass_policy() {
	max_day_check1=$(grep ^PASS_MAX_DAYS /etc/login.defs | wc -l)
	max_day_check2=$(grep ^PASS_MAX_DAYS /etc/login.defs | awk '($2==90)' | wc -l)
	
	min_day_check1=$(grep ^PASS_MIN_DAYS /etc/login.defs | wc -l)
	min_day_check2=$(grep ^PASS_MIN_DAYS /etc/login.defs | awk '($2==7)' | wc -l)
	
	warn_age_check1=$(grep ^PASS_WARN_AGE /etc/login.defs | wc -l)
	warn_age_check2=$(grep ^PASS_WARN_AGE /etc/login.defs | awk '($2==15)' | wc -l)
	
	min_len_check1=$(grep ^PASS_MIN_LEN /etc/login.defs | wc -l)
	min_len_check2=$(grep ^PASS_MIN_LEN /etc/login.defs | awk '($2>=8)' | wc -l)
	

	
	max_day_value=$(grep ^PASS_MAX_DAYS /etc/login.defs  |  tail -1 | awk '{print $2}')
	if [ $max_day_check1 -eq 0 ];then
		echo "PASS_MAX_DAYS 90" >> /etc/login.defs

		
	elif [ $max_day_check2 -eq 0 ]; then
		sed -i 's/^PASS_MAX_DAYS.*/PASS_MAX_DAYS 90/' /etc/login.defs
		
	fi
	
	min_day_value=$(grep ^PASS_MIN_DAYS /etc/login.defs  |  awk '{print $2}')
	if [ $min_day_check1 -eq 0 ];then
		echo "PASS_MIN_DAYS 7" >> /etc/login.defs
		
	elif [ $min_day_check2 -eq 0 ]; then
		sed -i 's/^PASS_MIN_DAYS.*/PASS_MIN_DAYS 7/' /etc/login.defs
		
	fi
	
	warn_age_value=$(grep ^PASS_WARN_AGE /etc/login.defs  |  awk '{print $2}')
	if [ $warn_age_check1 -eq 0 ];then
		echo "PASS_WARN_AGE 15" >> /etc/login.defs
		
	elif [ $warn_age_check2 -eq 0 ]; then
		sed -i 's/^PASS_WARN_AGE.*/PASS_WARN_AGE 15/' /etc/login.defs
		
	fi

	min_len_value=$(grep ^PASS_MIN_LEN /etc/login.defs | awk '{print $2}')
	if [ $min_len_check1 -eq 0 ];then
		echo "PASS_MIN_LEN	8" >> /etc/login.defs
		
	elif [ $min_len_check2 -eq 0 ]; then
		sed -i 's/^PASS_MIN_LEN.*/PASS_MIN_LEN	8/' /etc/login.defs
		
	fi
}


# 检查开机启动级别
runlevel_check() {
	if [ -x /bin/systemctl ];then
		runlevel_value=$(systemctl get-default )
		if [[ $runlevel_value != "multi-user.target" ]];then
			systemctl set-default multi-user.target &>/dev/null

		fi
	else
		runlevel_value=$(awk -F':' '/^id:.*initdefault/ {print $2}' /etc/inittab)
		if [[ $runlevel_value -ne 3 ]]; then
			sed -i /^id:/s/[[:digit:]]/3/ /etc/inittab
		fi
	fi

}


# 系统敏感文件权限设置：
passfile_check() {
	chown root:root /etc/passwd /etc/group /etc/shadow
	chmod 644 /etc/passwd /etc/group
	chmod 400 /etc/shadow
	echo -n
}

# 系统关键日志文件属性要求：
log_file_check() {
	[ -f /var/log/secure ] && chown root:root /var/log/secure && chmod 664 /var/log/secure
	[ -f /var/log/cron ] && chown root:root /var/log/cron && chmod 664 /var/log/cron
	[ -f /var/log/wtmp ] && chown root:root /var/log/wtmp && chmod 664 /var/log/wtmp
	[ -f /var/log/utmp ] && chown root:root /var/log/utmp && chmod 664 /var/log/utmp
	[ -f /var/log/lastlog ] && chown root:root /var/log/lastlog && chmod 664 /var/log/lastlog
	[ -f /var/log/messages ] && chown root:root /var/log/messages && chmod 664 /var/log/messages
	echo -n
}


# 开启安全日志记录功能
secure_log_check() {

	if [ $os_version -eq 5 ];then
		if [ -f /etc/init.d/rsyslog ]; then
			/etc/init.d/rsyslog status &>/dev/null
			if [ $? -eq 0 ];then
				auth_check=$(grep ^authpriv.* /var/log/secure /etc/rsyslog.conf | wc -l)
				
				if [[ $auth_check -eq 0 ]];then
				
					echo "authpriv.* /var/log/secure" >> /etc/rsyslog.conf
					/etc/init.d/rsyslog restart &>/dev/null

				fi
			else
				auth_check=$(grep ^authpriv.* /var/log/secure /etc/syslog.conf | wc -l)
				if [[ $auth_check -eq 0 ]];then

					echo "authpriv.* /var/log/secure" >> /etc/syslog.conf
					/etc/init.d/syslog restart &>/dev/null

				fi
			fi
			
		else
			auth_check=$(grep ^authpriv.* /var/log/secure /etc/syslog.conf | wc -l)
			if [[ $auth_check -eq 0 ]];then

				echo "authpriv.* /var/log/secure" >> /etc/syslog.conf
				/etc/init.d/syslog restart &>/dev/null

			fi
		fi

	else
		[ -f /etc/syslog.conf ] && \mv /etc/syslog.conf /etc/bak-syslog.conf
		auth_check=$(grep ^authpriv.* /var/log/secure /etc/rsyslog.conf | wc -l)
		if [[ $auth_check -eq 0 ]];then

			echo "authpriv.* /var/log/secure" >> /etc/rsyslog.conf
			if [ -x /bin/systemctl ];then
				systemctl restart rsyslog.service &>/dev/null
			else
				/etc/init.d/rsyslog restart &>/dev/null
			fi

		fi
	fi
}

# 执行修复基线

empty_pass && ssh_protocol_X11Forwarding && profile_check && pass_check && pass_policy && runlevel_check && passfile_check && log_file_check && secure_log_check && echo "Repair done." && echo $date_time > /root/repair_baseline_dir/repairdone

